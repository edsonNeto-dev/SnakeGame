package comum;
import java.io.Serializable;

public class EstadoCobra implements Serializable {
   
    private final int id;
    private final boolean viva;
    private final int pontos;
    private final int[] corpoX ;
    private final int[] corpoY ;


    public EstadoCobra(int id, boolean viva,int pontos,int[]corpoX,int[]corpoY) {
    this.id = id;
    this.viva = viva;
    this.pontos = pontos;
    this.corpoX= corpoX;
    this.corpoY=corpoY;
    }


    public int getId() {
        return id;
    }


    public boolean isViva() {
        return viva;
    }


    public int getPontos() {
        return pontos;
    }


    public int[] getCorpoX() {
        return corpoX;
    }


    public int[] getCorpoY() {
        return corpoY;
    }
    


}
