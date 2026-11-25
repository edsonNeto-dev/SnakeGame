package comum;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JogoRemoto extends Remote {

   int entrar() throws RemoteException;

    boolean enviarDirecao(int idJogador, Direcao direcao) throws RemoteException;

    EstadoJogo obterEstado() throws RemoteException;

    void sair(int idJogador) throws RemoteException;

}