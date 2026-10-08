package jogo.modelo;

/** Entidade com vida e velocidade. É a base de Player e Zombie. */
public abstract class Personagem extends Entidade {

    private int velocidade;
    private int pontosVida;

    protected Personagem(int x, int y, int largura, int altura,
                         int velocidade, int pontosVida) {
        super(x, y, largura, altura);
        this.velocidade = velocidade;
        this.pontosVida = pontosVida;
    }

    public int getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(int velocidade) {
        this.velocidade = velocidade;
    }

    public int getPontosVida() {
        return pontosVida;
    }

    public boolean isVivo() {
        return pontosVida > 0;
    }

    public void receberDano(int dano) {
        pontosVida = Math.max(0, pontosVida - dano);
    }

    protected void restaurarVida(int pontos) {
        pontosVida = pontos;
    }
}
