package servidor;

import comum.ConfigTabuleiro;

public class Partida {

    private static final int UNIDADES = ConfigTabuleiro.LARGURA_TELA * ConfigTabuleiro.ALTURA_TELA
            / (ConfigTabuleiro.TAMANHO_BLOCO * ConfigTabuleiro.TAMANHO_BLOCO);
    private final int[] eixoX = new int[UNIDADES];
    private final int[] eixoY = new int[UNIDADES];

}