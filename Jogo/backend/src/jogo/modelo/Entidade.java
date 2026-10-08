package jogo.modelo;

/**
 * Qualquer coisa que ocupa um retângulo na tela (player, zumbi...).
 * Guarda posição e tamanho e sabe calcular colisões.
 *
 * A posição é decimal (double) para o movimento ficar suave: com inteiros,
 * passos pequenos (velocidade x tempo do frame) seriam arredondados para 0.
 */
public abstract class Entidade {

    protected double x;
    protected double y;
    private final int largura;
    private final int altura;

    protected Entidade(int x, int y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    /** Posição arredondada, em pixels inteiros. */
    public int getX() {
        return (int) Math.round(x);
    }

    public int getY() {
        return (int) Math.round(y);
    }

    /** Posição exata (decimal). */
    public double getXExato() {
        return x;
    }

    public double getYExato() {
        return y;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    public double getCentroX() {
        return x + largura / 2.0;
    }

    public double getCentroY() {
        return y + altura / 2.0;
    }

    public void deslocar(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    /** Impede que a entidade saia da janela. */
    public void limitarA(Tela tela) {
        x = Math.max(0, Math.min(x, tela.getLargura() - largura));
        y = Math.max(0, Math.min(y, tela.getAltura() - altura));
    }

    /** Um ponto (ex.: um tiro) está dentro da hitbox? */
    public boolean contemPonto(double px, double py) {
        return px >= x && px <= x + largura
            && py >= y && py <= y + altura;
    }

    /** Duas hitboxes estão se tocando? */
    public boolean colideCom(Entidade outra) {
        return x < outra.x + outra.largura && x + largura > outra.x
            && y < outra.y + outra.altura && y + altura > outra.y;
    }
}
