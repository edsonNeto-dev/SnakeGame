package cliente;

import comum.Direcao;
import comum.EstadoJogo;
import comum.JogoRemoto;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ponto de entrada do cliente.
 *
 * Fluxo: pergunta o IP do servidor, busca o stub no RMI Registry, entra na
 * partida e abre a janela. A partir daí, duas threads conversam com o servidor:
 * uma busca o estado várias vezes por segundo (polling) e outra envia as
 * direções escolhidas pelo jogador. Nenhuma chamada remota é feita na thread do
 * Swing, para a janela não travar se a rede ficar lenta.
 *
 * Uso: java cliente.MainClient [ip-do-servidor] [porta]
 */
public class MainClient {

    /** Porta padrão do RMI Registry. */
    private static final int PORTA_PADRAO = 1099;

    /** Nome com que o servidor registra o objeto remoto. Tem que ser igual ao do MainServidor. */
    private static final String NOME_SERVICO = "SnakeGame";

    /** De quanto em quanto tempo o cliente pede o estado ao servidor. Menor que o tick (200 ms). */
    private static final int INTERVALO_ATUALIZACAO_MS = 100;

    private JogoRemoto jogo;
    private int meuId;
    private TelaJogo tela;

    /** Thread que busca o estado periodicamente. */
    private final ScheduledExecutorService atualizador = Executors.newSingleThreadScheduledExecutor();

    /** Thread única para as direções: assim elas chegam ao servidor na ordem em que foram apertadas. */
    private final ExecutorService envios = Executors.newSingleThreadExecutor();

    /** Garante que o tratamento de "conexão perdida" rode uma vez só. */
    private final AtomicBoolean desconectado = new AtomicBoolean(false);

    public static void main(String[] args) {
        // Sem isso, se o servidor cair, uma chamada remota pode ficar minutos
        // esperando antes de dar erro. Com 3 s, a queda é percebida rápido.
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "3000");
        System.setProperty("sun.rmi.transport.connectionTimeout", "3000");

        String host = args.length > 0 ? args[0] : perguntarHost();
        if (host == null || host.isBlank()) {
            return; // jogador cancelou
        }

        int porta = PORTA_PADRAO;
        if (args.length > 1) {
            try {
                porta = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                mostrarErro("Porta inválida: " + args[1]);
                return;
            }
        }

        new MainClient().iniciar(host.trim(), porta);
    }

    private void iniciar(String host, int porta) {
        // 1. Conectar: busca o stub no Registry e entra na partida.
        try {
            Registry registry = LocateRegistry.getRegistry(host, porta);
            jogo = (JogoRemoto) registry.lookup(NOME_SERVICO);
            meuId = jogo.entrar();
        } catch (NotBoundException e) {
            mostrarErro("O servidor em " + host + ":" + porta
                    + " não tem nenhum jogo registrado com o nome \"" + NOME_SERVICO + "\".");
            return;
        } catch (RemoteException e) {
            mostrarErro("Não foi possível conectar ao servidor em " + host + ":" + porta + ".\n\n"
                    + "Confira se o servidor está rodando, se o IP está certo\n"
                    + "e se o firewall libera a porta.\n\nDetalhe: " + e.getMessage());
            return;
        }

        if (meuId < 0) {
            mostrarErro("O servidor recusou a entrada (partida cheia ou já iniciada).");
            return;
        }

        // 2. Abrir a janela. invokeAndWait espera a tela existir antes de seguir,
        //    porque o atualizador vai usá-la logo em seguida.
        try {
            SwingUtilities.invokeAndWait(this::criarJanela);
        } catch (Exception e) {
            mostrarErro("Erro ao abrir a janela do jogo: " + e.getMessage());
            sairDoServidor();
            return;
        }

        // 3. Começar a buscar o estado periodicamente.
        atualizador.scheduleAtFixedRate(this::buscarEstado, 0, INTERVALO_ATUALIZACAO_MS, TimeUnit.MILLISECONDS);
    }

    private void criarJanela() {
        tela = new TelaJogo(meuId);
        LeitorDeTeclas leitor = new LeitorDeTeclas(this::enviarDirecao);
        tela.addKeyListener(leitor);
        tela.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                leitor.limpar();
            }
        });

        JFrame janela = new JFrame("SnakeGame — Jogador " + meuId);
        janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        janela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                encerrar();
            }
        });
        janela.add(tela);
        janela.setResizable(false);
        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
        tela.requestFocusInWindow();
    }

    /** Chamado pelo LeitorDeTeclas. A chamada remota roda na thread de envios. */
    private void enviarDirecao(Direcao direcao) {
        if (desconectado.get()) {
            return;
        }
        envios.submit(() -> {
            try {
                // O retorno diz se o servidor aceitou a direção. O cliente não
                // precisa fazer nada quando ela é recusada (ex.: meia-volta):
                // a cobra simplesmente continua no rumo que estava.
                jogo.enviarDirecao(meuId, direcao);
            } catch (RemoteException e) {
                conexaoPerdida();
            }
        });
    }

    /** Roda na thread do atualizador, a cada INTERVALO_ATUALIZACAO_MS. */
    private void buscarEstado() {
        try {
            EstadoJogo estado = jogo.obterEstado();
            SwingUtilities.invokeLater(() -> tela.atualizar(estado));
        } catch (RemoteException e) {
            conexaoPerdida();
        } catch (RuntimeException e) {
            // Uma exceção não tratada aqui cancelaria o agendamento em silêncio.
            e.printStackTrace();
        }
    }

    private void conexaoPerdida() {
        if (!desconectado.compareAndSet(false, true)) {
            return; // já tratado
        }
        atualizador.shutdown();
        envios.shutdownNow();
        SwingUtilities.invokeLater(() -> tela.mostrarAviso(
                "Conexão com o servidor perdida",
                "O servidor não respondeu. Feche a janela e conecte de novo."));
    }

    /** Fechamento da janela: avisa o servidor que o jogador saiu e encerra o programa. */
    private void encerrar() {
        atualizador.shutdownNow();
        envios.shutdownNow();
        if (!desconectado.get()) {
            sairDoServidor();
        }
        System.exit(0);
    }

    /**
     * Chama sair() numa thread separada e espera no máximo 1 segundo. Se o
     * servidor estiver fora do ar, a janela fecha mesmo assim.
     */
    private void sairDoServidor() {
        Thread saida = new Thread(() -> {
            try {
                jogo.sair(meuId);
            } catch (RemoteException ignorada) {
                // servidor já caiu: não há o que avisar
            }
        });
        saida.setDaemon(true);
        saida.start();
        try {
            saida.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String perguntarHost() {
        return (String) JOptionPane.showInputDialog(
                null,
                "IP do servidor (use localhost para jogar na mesma máquina):",
                "SnakeGame — Conectar",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "localhost");
    }

    private static void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "SnakeGame", JOptionPane.ERROR_MESSAGE);
    }
}
