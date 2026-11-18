package servidor;

import comum.Direcao;
import comum.ConfigTabuleiro;

public class Cobra {
    private int corpoCobra = 6;
    private int blocosComidos;
    private int id;
    private boolean viva = true;
    private Direcao direcao = Direcao.DIREITA;
    private Direcao ultimaDirecao = direcao;
    private static final int UNIDADES = ConfigTabuleiro.LARGURA_TELA * ConfigTabuleiro.ALTURA_TELA
            / (ConfigTabuleiro.TAMANHO_BLOCO * ConfigTabuleiro.TAMANHO_BLOCO);
    private final int[] eixoX = new int[UNIDADES];
    private final int[] eixoY = new int[UNIDADES];

    public Cobra(int id, int x, int y) {
        this.id = id;
        for (int i = 0; i < corpoCobra; i++) {
            eixoX[i] = x - i * ConfigTabuleiro.TAMANHO_BLOCO;
            eixoY[i] = y;
        }
    }

    public void andar() {
        for (int i = corpoCobra - 1; i > 0; i--) {
            eixoX[i] = eixoX[i - 1];
            eixoY[i] = eixoY[i - 1];
        }
        switch (direcao) {
            case CIMA:
                eixoY[0] = eixoY[0] - ConfigTabuleiro.TAMANHO_BLOCO;
                break;
            case BAIXO:
                eixoY[0] = eixoY[0] + ConfigTabuleiro.TAMANHO_BLOCO;
                break;
            case ESQUERDA:
                eixoX[0] = eixoX[0] - ConfigTabuleiro.TAMANHO_BLOCO;
                break;
            case DIREITA:
                eixoX[0] = eixoX[0] + ConfigTabuleiro.TAMANHO_BLOCO;
                break;

            default:
                break;
        }
        ultimaDirecao = direcao;

    }

    public boolean bateuBorda() {
        if (eixoX[0] < 0) {
            return true;
        }
        if (eixoY[0] < 0) {
            return true;
        }
        if (eixoX[0] > ConfigTabuleiro.LARGURA_TELA - ConfigTabuleiro.TAMANHO_BLOCO) {
            return true;
        }
        if (eixoY[0] > ConfigTabuleiro.ALTURA_TELA - ConfigTabuleiro.TAMANHO_BLOCO) {
            return true;
        }

        return false;
    }

public boolean bateuEmSiMesma(){
    for (int i = corpoCobra - 1; i > 0; i--) {
            if((eixoX[i]==eixoX[0]) && (eixoY[i]==eixoY[0])){
                return true;
            }
        }
        return false;
}
public boolean ocupaPosicao(int x,int y){
     for (int i = corpoCobra - 1; i >= 0; i--) {
        if((eixoX[i]==x) && (eixoY[i]==y)){
                return true;
            }

     }
        return false;
    }



    public void crescer() {
        if (corpoCobra < eixoX.length) {
            eixoX[corpoCobra] = eixoX[corpoCobra - 1];
            eixoY[corpoCobra] = eixoY[corpoCobra - 1];
            corpoCobra++;
        }
        blocosComidos++;

    }

    public boolean mudarDirecao(Direcao nova) {
        if (!viva) {
            return false;
        }
        if (nova == ultimaDirecao.oposta()) {
            return false;
        }
        direcao = nova;
        return true;
    }

    public void morrer() {
        viva = false;
    }

    public int getId() { return id; }

    public boolean isViva() { return viva; }

    public int getPontos() { return blocosComidos; }

    public int getCabecaX() { return eixoX[0]; }

    public int getCabecaY() { return eixoY[0]; }

    public int getTamanho() { return corpoCobra; }

}