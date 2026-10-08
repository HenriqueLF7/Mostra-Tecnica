package jogo.modelo;

/**
 * Qualquer coisa que ocupa um retângulo na tela (player, zumbi...).
 * Guarda posição e tamanho e sabe calcular colisões.
 */
public abstract class Entidade {

    protected int x;
    protected int y;
    private final int largura;
    private final int altura;

    protected Entidade(int x, int y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    public void deslocar(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    /** Impede que a entidade saia da janela. */
    public void limitarA(Tela tela) {
        x = Math.max(0, Math.min(x, tela.getLargura() - largura));
        y = Math.max(0, Math.min(y, tela.getAltura() - altura));
    }

    /** Um ponto (ex.: um tiro) está dentro da hitbox? */
    public boolean contemPonto(int px, int py) {
        return px >= x && px <= x + largura
            && py >= y && py <= y + altura;
    }

    /** Duas hitboxes estão se tocando? */
    public boolean colideCom(Entidade outra) {
        return x < outra.x + outra.largura && x + largura > outra.x
            && y < outra.y + outra.altura && y + altura > outra.y;
    }
}
