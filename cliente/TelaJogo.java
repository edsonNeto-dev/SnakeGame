package cliente;

import comum.ConfigTabuleiro;
import comum.EstadoCobra;
import comum.EstadoJogo;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Painel que desenha o tabuleiro a partir do último estado recebido do servidor.
 *
 * A tela não guarda nenhuma regra do jogo: ela só desenha o EstadoJogo que o
 * MainClient entrega pelo método atualizar(). Todas as posições já chegam em
 * pixels, do jeito que o servidor guarda.
 */
public class TelaJogo extends JPanel {

    private static final int BLOCO = ConfigTabuleiro.TAMANHO_BLOCO;
    private static final int LARGURA = ConfigTabuleiro.LARGURA_TELA;
    private static final int ALTURA = ConfigTabuleiro.ALTURA_TELA;

    /** Faixa no topo da janela com o placar, acima do tabuleiro. */
    private static final int ALTURA_PLACAR = 48;

    /** Espaço entre os blocos, para cada pedaço da cobra aparecer separado. */
    private static final int FOLGA = 3;

    // Paleta
    private static final Color FUNDO_PLACAR = new Color(28, 33, 40);
    private static final Color TEXTO_PLACAR = new Color(230, 234, 238);
    private static final Color TEXTO_SECUNDARIO = new Color(150, 160, 172);
    private static final Color CASA_CLARA = new Color(170, 215, 81);
    private static final Color CASA_ESCURA = new Color(162, 209, 73);
    private static final Color COR_COMIDA = new Color(231, 71, 29);
    private static final Color COR_MORTA = new Color(120, 120, 120, 140);
    private static final Color VEU = new Color(0, 0, 0, 150);

    /** Uma cor por jogador, escolhida pelo id. */
    private static final Color[] CORES_JOGADORES = {
            new Color(66, 103, 210),  // azul
            new Color(155, 66, 196),  // roxo
            new Color(232, 140, 30),  // laranja
            new Color(20, 150, 140),  // verde-água
            new Color(214, 51, 108),  // rosa
            new Color(90, 90, 90),    // grafite
    };

    private static final Font FONTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    private static final Font FONTE_PLACAR = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    private static final Font FONTE_PLACAR_NEGRITO = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FONTE_AVISO = new Font(Font.SANS_SERIF, Font.BOLD, 42);
    private static final Font FONTE_AVISO_DETALHE = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

    /** Id que o servidor devolveu no entrar(); usado para destacar "Você". */
    private final int meuId;

    // volatile: estas variáveis são escritas pela thread que fala com o servidor
    // e lidas pela thread do Swing que desenha a tela.
    private volatile EstadoJogo estado;
    private static final String AVISO_CONECTANDO = "Conectando ao servidor...";
    private volatile String aviso = AVISO_CONECTANDO;
    private volatile String avisoDetalhe = null;

    public TelaJogo(int meuId) {
        this.meuId = meuId;
        setPreferredSize(new Dimension(LARGURA, ALTURA + ALTURA_PLACAR));
        setBackground(FUNDO_PLACAR);
        setFocusable(true);
        setDoubleBuffered(true);
    }

    /** Recebe um novo estado do servidor e pede para a tela ser redesenhada. */
    public void atualizar(EstadoJogo novoEstado) {
        this.estado = novoEstado;
        if (AVISO_CONECTANDO.equals(aviso)) { // some assim que o primeiro estado chega
            aviso = null;
        }
        repaint();
    }

    /** Mostra um aviso em cima do tabuleiro (ex.: conexão perdida). */
    public void mostrarAviso(String titulo, String detalhe) {
        this.aviso = titulo;
        this.avisoDetalhe = detalhe;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Lê o estado uma vez só: se chegar outro no meio do desenho,
        // esta pintura continua usando o mesmo, sem misturar dois ticks.
        EstadoJogo atual = estado;

        desenharPlacar(g2, atual);

        g2.translate(0, ALTURA_PLACAR);
        desenharTabuleiro(g2);

        if (atual != null) {
            desenharComida(g2, atual.getComidaX(), atual.getComidaY());

            // Outras cobras primeiro e a do jogador por último, para ela ficar por cima.
            // Cobras mortas não são desenhadas: o corpo some do tabuleiro.
            EstadoCobra minha = null;
            for (EstadoCobra cobra : atual.getCobras()) {
                if (cobra.getId() == meuId) {
                    minha = cobra;
                } else if (cobra.isViva()) {
                    desenharCobra(g2, cobra);
                }
            }
            if (minha != null && minha.isViva()) {
                desenharCobra(g2, minha);
            }

            if (aviso == null && minha != null && !minha.isViva()) {
                desenharAviso(g2, "Você morreu",
                        "Pontuação final: " + minha.getPontos() + "  ·  feche a janela para sair");
            }
        }

        if (aviso != null) {
            desenharAviso(g2, aviso, avisoDetalhe);
        }

        g2.dispose();
    }

    // ---------------------------------------------------------------- placar

    private void desenharPlacar(Graphics2D g2, EstadoJogo atual) {
        g2.setColor(FUNDO_PLACAR);
        g2.fillRect(0, 0, LARGURA, ALTURA_PLACAR);

        int baseTexto = ALTURA_PLACAR / 2 + 7;
        g2.setFont(FONTE_TITULO);
        g2.setColor(TEXTO_PLACAR);
        g2.drawString("SnakeGame", 20, baseTexto);

        if (atual == null) {
            return;
        }

        // Jogadores da direita para a esquerda, a partir da borda direita.
        int x = LARGURA - 20;
        List<EstadoCobra> cobras = atual.getCobras();
        for (int i = cobras.size() - 1; i >= 0; i--) {
            EstadoCobra cobra = cobras.get(i);
            boolean eu = cobra.getId() == meuId;

            String nome = eu ? "Você" : "Jogador " + cobra.getId();
            String pontos = cobra.getPontos() + (cobra.getPontos() == 1 ? " pt" : " pts");

            g2.setFont(FONTE_PLACAR);
            FontMetrics fmPontos = g2.getFontMetrics();
            g2.setFont(eu ? FONTE_PLACAR_NEGRITO : FONTE_PLACAR);
            FontMetrics fmNome = g2.getFontMetrics();

            int larguraItem = 14 + 8 + fmNome.stringWidth(nome) + 8 + fmPontos.stringWidth(pontos);
            x -= larguraItem;

            // Bolinha com a cor do jogador (vazada se a cobra morreu).
            Color cor = corDoJogador(cobra.getId());
            g2.setColor(cor);
            if (cobra.isViva()) {
                g2.fillOval(x, baseTexto - 12, 14, 14);
            } else {
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x + 1, baseTexto - 11, 12, 12);
            }

            g2.setColor(cobra.isViva() ? TEXTO_PLACAR : TEXTO_SECUNDARIO);
            g2.drawString(nome, x + 22, baseTexto);

            g2.setFont(FONTE_PLACAR);
            g2.setColor(TEXTO_SECUNDARIO);
            g2.drawString(pontos, x + 22 + fmNome.stringWidth(nome) + 8, baseTexto);

            x -= 28; // espaço entre jogadores
        }
    }

    // ------------------------------------------------------------- tabuleiro

    /** Fundo quadriculado, uma casa por bloco, para facilitar enxergar a grade. */
    private void desenharTabuleiro(Graphics2D g2) {
        for (int coluna = 0; coluna * BLOCO < LARGURA; coluna++) {
            for (int linha = 0; linha * BLOCO < ALTURA; linha++) {
                g2.setColor((coluna + linha) % 2 == 0 ? CASA_CLARA : CASA_ESCURA);
                g2.fillRect(coluna * BLOCO, linha * BLOCO, BLOCO, BLOCO);
            }
        }
    }

    private void desenharComida(Graphics2D g2, int x, int y) {
        int margem = 7;
        g2.setColor(COR_COMIDA);
        g2.fillOval(x + margem, y + margem, BLOCO - 2 * margem, BLOCO - 2 * margem);

        // brilho e cabinho, só para parecer uma fruta
        g2.setColor(new Color(255, 255, 255, 110));
        g2.fillOval(x + margem + 7, y + margem + 6, 9, 7);
        g2.setColor(new Color(90, 60, 30));
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + BLOCO / 2, y + margem + 2, x + BLOCO / 2 + 4, y + margem - 4);
    }

    private void desenharCobra(Graphics2D g2, EstadoCobra cobra) {
        int[] xs = cobra.getCorpoX();
        int[] ys = cobra.getCorpoY();
        if (xs.length == 0) {
            return;
        }

        Color corCorpo = cobra.isViva() ? corDoJogador(cobra.getId()) : COR_MORTA;
        Color corCabeca = cobra.isViva() ? corCorpo.darker() : COR_MORTA.darker();
        int arco = 18;

        // Do rabo para a cabeça, assim a cabeça é desenhada por cima.
        for (int i = xs.length - 1; i >= 1; i--) {
            g2.setColor(corCorpo);
            g2.fillRoundRect(xs[i] + FOLGA, ys[i] + FOLGA, BLOCO - 2 * FOLGA, BLOCO - 2 * FOLGA, arco, arco);
        }

        g2.setColor(corCabeca);
        g2.fillRoundRect(xs[0] + FOLGA, ys[0] + FOLGA, BLOCO - 2 * FOLGA, BLOCO - 2 * FOLGA, arco, arco);

        // Contorno branco na cobra do próprio jogador, para ele se achar na tela.
        if (cobra.getId() == meuId && cobra.isViva()) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(xs[0] + FOLGA, ys[0] + FOLGA, BLOCO - 2 * FOLGA, BLOCO - 2 * FOLGA, arco, arco);
        }

        if (cobra.isViva()) {
            desenharOlhos(g2, xs, ys);
        }
    }

    /**
     * Desenha os olhos virados para onde a cobra está indo. A direção é deduzida
     * comparando a cabeça com o pedaço logo atrás dela.
     */
    private void desenharOlhos(Graphics2D g2, int[] xs, int[] ys) {
        int dx = 1;
        int dy = 0;
        if (xs.length > 1) {
            dx = Integer.signum(xs[0] - xs[1]);
            dy = Integer.signum(ys[0] - ys[1]);
            if (dx == 0 && dy == 0) {
                dx = 1; // pedaços sobrepostos (logo após crescer): assume direita
            }
        }

        int centroX = xs[0] + BLOCO / 2;
        int centroY = ys[0] + BLOCO / 2;
        int avanco = 9;   // quanto os olhos vão para a frente da cabeça
        int abertura = 9; // distância de cada olho até o meio

        // A "frente" é (dx, dy); a lateral é a perpendicular (-dy, dx).
        int[][] olhos = {
                {centroX + dx * avanco - dy * abertura, centroY + dy * avanco + dx * abertura},
                {centroX + dx * avanco + dy * abertura, centroY + dy * avanco - dx * abertura},
        };

        for (int[] olho : olhos) {
            g2.setColor(Color.WHITE);
            g2.fillOval(olho[0] - 6, olho[1] - 6, 12, 12);
            g2.setColor(Color.BLACK);
            g2.fillOval(olho[0] - 3 + dx * 2, olho[1] - 3 + dy * 2, 6, 6);
        }
    }

    // ----------------------------------------------------------------- avisos

    private void desenharAviso(Graphics2D g2, String titulo, String detalhe) {
        g2.setColor(VEU);
        g2.fillRect(0, 0, LARGURA, ALTURA);

        FontMetrics fmTitulo = g2.getFontMetrics(FONTE_AVISO);
        FontMetrics fmDetalhe = g2.getFontMetrics(FONTE_AVISO_DETALHE);

        // Caixa escura atrás do texto, para ele não se misturar com as cobras.
        int larguraTexto = fmTitulo.stringWidth(titulo);
        if (detalhe != null) {
            larguraTexto = Math.max(larguraTexto, fmDetalhe.stringWidth(detalhe));
        }
        int larguraCaixa = larguraTexto + 80;
        int alturaCaixa = detalhe != null ? 130 : 90;
        int caixaX = (LARGURA - larguraCaixa) / 2;
        int caixaY = (ALTURA - alturaCaixa) / 2;
        g2.setColor(new Color(28, 33, 40, 235));
        g2.fillRoundRect(caixaX, caixaY, larguraCaixa, alturaCaixa, 24, 24);

        int y = caixaY + 58;
        g2.setColor(Color.WHITE);
        g2.setFont(FONTE_AVISO);
        g2.drawString(titulo, (LARGURA - fmTitulo.stringWidth(titulo)) / 2, y);

        if (detalhe != null) {
            g2.setFont(FONTE_AVISO_DETALHE);
            g2.setColor(TEXTO_SECUNDARIO);
            g2.drawString(detalhe, (LARGURA - fmDetalhe.stringWidth(detalhe)) / 2, y + 42);
        }
    }

    private static Color corDoJogador(int id) {
        return CORES_JOGADORES[Math.floorMod(id - 1, CORES_JOGADORES.length)];
    }
}
