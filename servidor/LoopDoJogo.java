package servidor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoopDoJogo {
    private static final int INTERVALO = 200;

    private final Partida partida;
    private final ScheduledExecutorService agendador = Executors.newSingleThreadScheduledExecutor();//Cria um agendador que usa uma unica thread, ou seja so roda uma partida por vez

    public LoopDoJogo(Partida partida) {
        this.partida = partida;
    }

    public void iniciarJogo() {
        agendador.scheduleAtFixedRate(() -> {
            // sem o try/catch, uma exceção cancelaria as repetições sem aviso
            try {
                partida.avancarPartida();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, INTERVALO, TimeUnit.MILLISECONDS);
    }

}
