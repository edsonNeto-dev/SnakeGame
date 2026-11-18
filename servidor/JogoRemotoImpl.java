package servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import comum.Direcao;
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
    public boolean enviarDirecao(Direcao direcao) throws RemoteException {

        return true;
    }

    @Override
    public void obterEstado() throws RemoteException {

    }

    @Override
    public void sair() throws RemoteException {

    }

}