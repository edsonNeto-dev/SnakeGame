package servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import comum.Direcao;
import comum.EstadoJogo;
import comum.JogoRemoto;

public class JogoRemotoImpl extends UnicastRemoteObject implements JogoRemoto {

      public JogoRemotoImpl() throws RemoteException{
        super();
      }


    @Override
      public int entrar() throws RemoteException {
        return 1;
    }

    @Override
    public boolean enviarDirecao(int idJogador, Direcao direcao) throws RemoteException {

        return true;
    }

    @Override
    public EstadoJogo obterEstado() throws RemoteException {
        return new EstadoJogo(0, 0, new ArrayList<>());
    }

    @Override
    public void sair(int idJogador) throws RemoteException {

    }

}