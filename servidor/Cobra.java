package servidor;

import comum.ConfigTabuleiro;

public class Cobra {
    private int corpoCobra = 6;
    private int blocosComidos;
    private char direcao = 'D';// C - Cima, B- Baixo,E- Esquerda, D- Direita
    private static final int UNIDADES = ConfigTabuleiro.LARGURA_TELA * ConfigTabuleiro.ALTURA_TELA
            / (ConfigTabuleiro.TAMANHO_BLOCO * ConfigTabuleiro.TAMANHO_BLOCO);
    private final int[] eixoX = new int[UNIDADES];
    private final int[] eixoY = new int[UNIDADES];

    public void andar() {

    }
    public void actionPerformed(ActionEvent e){
        
    }

}
