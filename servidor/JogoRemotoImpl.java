package servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import comum.Direcao;
import comum.EstadoJogo;
import comum.JogoRemoto;

public class JogoRemotoImpl extends UnicastRemoteObject implements JogoRemoto {
    // porta fixa do objeto remoto, para liberar no firewall
    public static final int PORTA_OBJETO = 1100;

    private final Partida partida;

    public JogoRemotoImpl(Partida partida) throws RemoteException {
        super(PORTA_OBJETO);
        this.partida = partida;
    }

    @Override
    public int entrar() throws RemoteException {
        int id = partida.adicionarJogador();
        if (id == -1) {
            System.out.println("Entrada recusada: partida cheia");
        } else {
            System.out.println("Jogador " + id + " entrou");
        }
        return id;
    }

    @Override
    public boolean enviarDirecao(int idJogador, Direcao direcao) throws RemoteException {
        return partida.mudarDirecao(idJogador, direcao);
    }

    @Override
    public EstadoJogo obterEstado() throws RemoteException {
        return partida.gerarEstado();
    }

    @Override
    public void sair(int idJogador) throws RemoteException {
        partida.removerJogador(idJogador);
        System.out.println("Jogador " + idJogador + " saiu");
    }

}
