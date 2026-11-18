package comum;

public enum Direcao {
    CIMA, BAIXO, ESQUERDA, DIREITA;

    public Direcao oposta() {
        switch (this) {
            case CIMA:
                return BAIXO;
            case BAIXO:
                return CIMA;
            case ESQUERDA:
                return DIREITA;
            case DIREITA:
                return ESQUERDA;
            default:
                throw new IllegalStateException("Direção desconhecida: " + this);
        }
    }
}
