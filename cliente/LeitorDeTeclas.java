package cliente;

import comum.Direcao;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Traduz as teclas do jogador em direções e repassa cada uma para quem vai
 * enviá-la ao servidor.
 *
 * O cliente não valida jogadas (por exemplo, a meia-volta): essa regra fica
 * no servidor, que é a autoridade do jogo.
 */
public class LeitorDeTeclas extends KeyAdapter {

    /** Quem recebe a direção escolhida (o MainClient, que faz a chamada remota). */
    private final Consumer<Direcao> aoEscolherDirecao;

    /**
     * Teclas que estão sendo seguradas agora. Quando o jogador mantém uma tecla
     * pressionada, o sistema repete o evento várias vezes por segundo; guardando
     * as teclas seguradas, cada aperto gera um único envio pela rede.
     */
    private final Set<Integer> teclasSeguradas = new HashSet<>();

    public LeitorDeTeclas(Consumer<Direcao> aoEscolherDirecao) {
        this.aoEscolherDirecao = aoEscolherDirecao;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int codigo = e.getKeyCode();

        // add() devolve false se a tecla já estava segurada: é repetição automática.
        if (!teclasSeguradas.add(codigo)) {
            return;
        }

        Direcao direcao = traduzir(codigo);
        if (direcao != null) {
            aoEscolherDirecao.accept(direcao);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        teclasSeguradas.remove(e.getKeyCode());
    }

    /**
     * Esquece as teclas seguradas. Chamado quando a janela perde o foco, porque
     * nesse caso o keyReleased nunca chega e a tecla ficaria "presa".
     */
    public void limpar() {
        teclasSeguradas.clear();
    }

    /** Setas ou WASD. Qualquer outra tecla devolve null e é ignorada. */
    private static Direcao traduzir(int codigo) {
        switch (codigo) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                return Direcao.CIMA;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                return Direcao.BAIXO;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                return Direcao.ESQUERDA;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                return Direcao.DIREITA;
            default:
                return null;
        }
    }
}
