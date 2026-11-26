package servidor;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainServidor {
    private static final int PORTA_REGISTRY = 1099;
    private static final String NOME_SERVICO = "SnakeGame"; // igual ao NOME_SERVICO do MainClient

    public static void main(String[] args) {
        try {
            // a mesma partida vai para a JogoRemotoImpl e para o loop
            Partida partida = new Partida();
            JogoRemotoImpl jogo = new JogoRemotoImpl(partida);
            LoopDoJogo loop = new LoopDoJogo(partida);

            Registry registry = LocateRegistry.createRegistry(PORTA_REGISTRY);
            registry.rebind(NOME_SERVICO, jogo);

            loop.iniciarJogo();
            System.out.println("Servidor pronto na porta " + PORTA_REGISTRY);
        } catch (RemoteException e) {
            System.out.println("Não foi possível iniciar o servidor: porta "
                    + PORTA_REGISTRY + " ou " + JogoRemotoImpl.PORTA_OBJETO + " em uso?");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
