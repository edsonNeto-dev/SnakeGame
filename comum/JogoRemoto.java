package comum;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JogoRemoto extends Remote {
   
    void entrar() throws RemoteException;

    boolean enviarDirecao(Direcao direcao) throws RemoteException ;

    void obterEstado() throws RemoteException;

    void sair() throws RemoteException;

}