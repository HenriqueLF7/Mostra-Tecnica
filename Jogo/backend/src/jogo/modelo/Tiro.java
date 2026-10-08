package jogo.modelo;

import jogo.util.JsonObject;
import jogo.util.JsonSerializavel;

public class Tiro implements JsonSerializavel {

    // Posição da ponta da arma em relação ao player
    public static final int MUZZLE_OFFSET_X = 86;
    public static final int MUZZLE_OFFSET_Y = 52;

    private static final double VELOCIDADE = 50;
    private static final int DANO = 1;

    private double x;
    private double y;
    private final double dirX;      // vetor normalizado
    private final double dirY;
    private final double angulo;    // em graus, pro front girar o sprite
    private final Lado lado;
    private boolean ativo = true;

    private Tiro(double x, double y, double alvoX, double alvoY, Lado lado) {
        this.x = x;
        this.y = y;
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
                player.getX() + offsetX,
                player.getY() + MUZZLE_OFFSET_Y,
                alvoX,
                alvoY,
                lado
        );
    }

    public void mover() {
        if (!ativo) {
            return;
        }

        x += dirX * VELOCIDADE;
        y += dirY * VELOCIDADE;
    }

    public boolean saiuDaTela(Tela tela) {
        boolean fora = x < 0 || x > tela.getLargura()
                    || y < 0 || y > tela.getAltura();

        if (fora) {
            ativo = false;
        }

        return fora;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
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
                .campo("x", (int) x)
                .campo("y", (int) y)
                .campo("angulo", (int) angulo)
                .campo("direcao", lado.texto())
                .construir();
    }
}
