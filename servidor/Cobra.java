package servidor;
import  comum.Direcao;
import comum.ConfigTabuleiro;
import comum.Direcao;

public class Cobra {
    private int corpoCobra = 6;
    private int blocosComidos;
    private Direcao direcao = Direcao.DIREITA;// C - Cima, B- Baixo,E- Esquerda, D- Direita
    private static final int UNIDADES = ConfigTabuleiro.LARGURA_TELA * ConfigTabuleiro.ALTURA_TELA
            / (ConfigTabuleiro.TAMANHO_BLOCO * ConfigTabuleiro.TAMANHO_BLOCO);
    private final int[] eixoX = new int[UNIDADES];
    private final int[] eixoY = new int[UNIDADES];

    public void andar() {

    }

    public void crescerCobra() {

    }

}
