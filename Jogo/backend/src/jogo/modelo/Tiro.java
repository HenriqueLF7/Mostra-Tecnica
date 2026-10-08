package jogo.modelo;

import jogo.util.JsonObject;
import jogo.util.JsonSerializavel;

import java.util.concurrent.atomic.AtomicInteger;

public class Tiro implements JsonSerializavel {

    // Posição da ponta da arma em relação ao player
    public static final int MUZZLE_OFFSET_X = 86;
    public static final int MUZZLE_OFFSET_Y = 52;

    /** Pixels por SEGUNDO. */
    private static final double VELOCIDADE = 1000;
    private static final int DANO = 1;

    private static final AtomicInteger PROXIMO_ID = new AtomicInteger(1);

    private final int id = PROXIMO_ID.getAndIncrement();

    private double x;
    private double y;
    private double xAnterior;
    private double yAnterior;
    private final double dirX;      // vetor normalizado
    private final double dirY;
    private final double angulo;    // em graus, pro front girar o sprite
    private final Lado lado;
    private boolean ativo = true;

    private Tiro(double x, double y, double alvoX, double alvoY, Lado lado) {
        this.x = x;
        this.y = y;
        this.xAnterior = x;
        this.yAnterior = y;
        this.lado = lado;

        double dx = alvoX - x;
        double dy = alvoY - y;
        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia < 1) {
            // mouse exatamente na ponta da arma: atira pro lado que o boneco olha
            this.dirX = lado == Lado.ESQUERDA ? -1 : 1;
            this.dirY = 0;
        } else {
            this.dirX = dx / distancia;
            this.dirY = dy / distancia;
        }

        this.angulo = Math.toDegrees(Math.atan2(dirY, dirX));
    }

    /** Nasce na ponta da arma do player e voa em direção ao alvo (mouse). */
    public static Tiro naPontaDaArma(Player player, int alvoX, int alvoY, Lado lado) {
        int offsetX = lado == Lado.ESQUERDA
                ? Player.LARGURA - MUZZLE_OFFSET_X
                : MUZZLE_OFFSET_X;

        return new Tiro(
                player.getXExato() + offsetX,
                player.getYExato() + MUZZLE_OFFSET_Y,
                alvoX,
                alvoY,
                lado
        );
    }

    /** Anda durante dt segundos. Guarda a posição anterior para testar colisão no caminho todo. */
    public void mover(double dt) {
        if (!ativo) {
            return;
        }

        xAnterior = x;
        yAnterior = y;

        x += dirX * VELOCIDADE * dt;
        y += dirY * VELOCIDADE * dt;
    }

    public boolean saiuDaTela(Tela tela) {
        boolean fora = x < 0 || x > tela.getLargura()
                    || y < 0 || y > tela.getAltura();

        if (fora) {
            ativo = false;
        }

        return fora;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getXAnterior() {
        return xAnterior;
    }

    public double getYAnterior() {
        return yAnterior;
    }

    public int getDano() {
        return DANO;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public String toJson() {
        return JsonObject.novo()
                .campo("id", id)
                .campo("x", x)
                .campo("y", y)
                .campo("vx", (int) Math.round(dirX * VELOCIDADE))
                .campo("vy", (int) Math.round(dirY * VELOCIDADE))
                .campo("angulo", (int) angulo)
                .campo("direcao", lado.texto())
                .construir();
    }
}
