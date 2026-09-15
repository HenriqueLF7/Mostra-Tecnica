public class Zombie {

    private int x;
    private int y;
    private int velocidade;
    private boolean vida = true;

    public Zombie(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocidade = 20;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

public void moverEmDirecao(int alvoX, int alvoY) {

    if (!vida) return;
        if (x < alvoX) x += velocidade;
        if (x > alvoX) x -= velocidade;
        if (y < alvoY) y += velocidade;
        if (y > alvoY) y -= velocidade;
    }
}