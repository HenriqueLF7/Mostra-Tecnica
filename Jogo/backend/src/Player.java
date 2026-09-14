public class Player {

    private int x;
    private int y;
    private int velocidade;
    private int pontosVida;
    private boolean vida = true;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocidade = 15;
        this.pontosVida = 3;
    }

    public void vidaPlayer(int pontos) {
        this.pontosVida += pontos;
        if (this.pontosVida <= 0) {
            this.pontosVida = 0;
            this.vida = false;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPontosVida() {
        return pontosVida;
    }

    public boolean isVivo() {
        return vida;
    }

public void movimentacaoPlayer(String comando, int larguraJanela, int alturaJanela) {

    if (!vida) return;

    switch (comando) {
        case "W":
            y -= velocidade;
            break;
        case "S":
            y += velocidade;
            break;
        case "A":
            x -= velocidade;
            break;
        case "D":
            x += velocidade;
            break;
    }

    // Limita a movimentação dentro da janela atual do navegador
    int larguraMax = larguraJanela - 90;
    int alturaMax = alturaJanela - 100;

    if (x < 0) x = 0;
    if (y < 0) y = 0;
    if (x > larguraMax) x = larguraMax;
    if (y > alturaMax) y = alturaMax;
    }
}