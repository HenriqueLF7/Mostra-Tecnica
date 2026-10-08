package jogo.modelo;

/** Tamanho da janela do navegador (objeto imutável). */
public final class Tela {

    private final int largura;
    private final int altura;

    public Tela(int largura, int altura) {
        this.largura = largura;
        this.altura = altura;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }
}
