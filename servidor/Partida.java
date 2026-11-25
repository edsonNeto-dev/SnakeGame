package servidor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import comum.ConfigTabuleiro;
import comum.Direcao;
import comum.EstadoCobra;
import comum.EstadoJogo;

public class Partida {
    private static final int MAX_JOGADORES = 4;
    private static final int X_INICIAL = 5 * ConfigTabuleiro.TAMANHO_BLOCO;
    private static final int TAMANHO_INICIAL_COBRA = 6; // mesmo valor de corpoCobra na Cobra
    private static final int[] LINHAS_INICIAIS = { 3, 6, 9, 12 };

    private final Map<Integer, Cobra> cobras = new LinkedHashMap<>();
    private int proximoId = 1;
    private final Random random = new Random();
    private int blocoX;
    private int blocoY;

    public Partida() {
        criarBloco();
    }

    public synchronized void avancarPartida() {
        for (Cobra cobra : cobras.values()) {
            if (cobra.isViva()) {
                cobra.andar();
            }
        }
        validarLimite();
        alcancarBloco();
    }

    // a cobra que encostar a cabeça na comida cresce e a comida é sorteada de novo
    private void alcancarBloco() {
        for (Cobra cobra : cobras.values()) {
            if (cobra.isViva() && cobra.getCabecaX() == blocoX && cobra.getCabecaY() == blocoY) {
                cobra.crescer();
                criarBloco();
            }
        }
    }

    // primeiro descobre quem morreu e só depois mata, para a ordem do mapa não mudar o resultado
    private void validarLimite() {
        List<Cobra> mortas = new ArrayList<>();
        for (Cobra cobra : cobras.values()) {
            if (cobra.isViva() && (cobra.bateuBorda() || cobra.bateuEmSiMesma() || bateuEmOutra(cobra))) {
                mortas.add(cobra);
            }
        }
        for (Cobra cobra : mortas) {
            cobra.morrer();
        }
    }

    private boolean bateuEmOutra(Cobra cobra) {
        for (Cobra outra : cobras.values()) {
            if (outra != cobra && outra.isViva() && outra.ocupaPosicao(cobra.getCabecaX(), cobra.getCabecaY())) {
                return true;
            }
        }
        return false;
    }
    // Cria a o bloco de comida no tabuleiro de forma aleatoria 
    private void criarBloco() {
        int colunas = ConfigTabuleiro.LARGURA_TELA / ConfigTabuleiro.TAMANHO_BLOCO;
        int linhas = ConfigTabuleiro.ALTURA_TELA / ConfigTabuleiro.TAMANHO_BLOCO;
       //cria o bloco de comida se a cobra nao estiver passando pela posicao, se a posicao estiver ocupada ele sorteia novamente  
        do {
            blocoX = random.nextInt(colunas) * ConfigTabuleiro.TAMANHO_BLOCO;
            blocoY = random.nextInt(linhas) * ConfigTabuleiro.TAMANHO_BLOCO;
        } while (posicaoOcupada(blocoX, blocoY));
    }
       //pergunta para a cobra se ela esta naquela posicao 
    private boolean posicaoOcupada(int x, int y) {
        for (Cobra cobra : cobras.values()) {
            if (cobra.isViva() && cobra.ocupaPosicao(x, y)) {
                return true;
            }
        }
        return false;
    }
    //adiciona o jogador no tabuleiro, olhando o limite maximo de jogadores 
    public synchronized int adicionarJogador() {
        if (cobras.size() >= MAX_JOGADORES) {
            return -1;
        }
    // confirma se nao tem outra cobra na posicao atual para adicionar o novo jogador 
        for (int linha : LINHAS_INICIAIS) {
            int y = linha * ConfigTabuleiro.TAMANHO_BLOCO;
            if (linhaLivre(y)) {
                int id = proximoId;
                proximoId++;
                cobras.put(id, new Cobra(id, X_INICIAL, y));
                return id;
            }
        }
        return -1;
    }

    private boolean linhaLivre(int y) {
        for (Cobra cobra : cobras.values()) {
            if (!cobra.isViva()) {
                continue;
            }
            for (int i = 0; i < TAMANHO_INICIAL_COBRA; i++) {
                int x = X_INICIAL - i * ConfigTabuleiro.TAMANHO_BLOCO;
                if (cobra.ocupaPosicao(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized void removerJogador(int id) {
        cobras.remove(id);
    }

    public synchronized boolean mudarDirecao(int id, Direcao direcao) {
        Cobra cobra = cobras.get(id);
        if (cobra == null) {
            return false;
        }
        return cobra.mudarDirecao(direcao);
    }
    //tira uma foto do jogo inteiro atual, de todas as cobras, posicao da comida
    public synchronized EstadoJogo gerarEstado() {
        List<EstadoCobra> estados = new ArrayList<>();
        for (Cobra cobra : cobras.values()) {
            estados.add(cobra.gerarEstado());
        }
        return new EstadoJogo(blocoX, blocoY, estados);
    }

}
