package jogo.modelo;

/** Os quatro movimentos possíveis do player (teclas W, A, S, D). */
public enum Movimento {

    CIMA("W", 0, -1),
    BAIXO("S", 0, 1),
    ESQUERDA("A", -1, 0),
    DIREITA("D", 1, 0);

    private final String comando;
    private final int dx;
    private final int dy;

    Movimento(String comando, int dx, int dy) {
        this.comando = comando;
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    /** Retorna o movimento do comando ("W", "A", "S" ou "D") ou null se for desconhecido. */
    public static Movimento deComando(String comando) {
        for (Movimento m : values()) {
            if (m.comando.equalsIgnoreCase(comando)) {
                return m;
            }
        }
        return null;
    }
}
