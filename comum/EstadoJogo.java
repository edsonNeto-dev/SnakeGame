package comum;

import java.io.Serializable;
import java.util.List;

public class EstadoJogo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int comidaX;
    private final int comidaY;
    private final List<EstadoCobra> cobras;

    public EstadoJogo(int comidaX, int comidaY, List<EstadoCobra> cobras) {
        this.comidaX = comidaX;
        this.comidaY = comidaY;
        this.cobras = cobras;
    }

    public int getComidaX() {
        return comidaX;
    }

    public int getComidaY() {
        return comidaY;
    }

    public List<EstadoCobra> getCobras() {
        return cobras;
    }

}
