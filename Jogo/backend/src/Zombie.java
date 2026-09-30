public class Zombie {

    // Tamanho da hitbox. Ajuste pra bater com #zumbi no style.css
    public static int LARGURA = 57;
    public static int ALTURA = 95;

    private int x;
    private int y;
    private int velocidade;
    private int pontosVida;
    private boolean vida = true;

    public Zombie(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocidade = 10;
        this.pontosVida = 1;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isVivo() { return vida; }

    public void receberDano(int dano) {
        pontosVida -= dano;
        if (pontosVida <= 0) {
            pontosVida = 0;
            vida = false;
        }
    }

    // Nasce numa borda da tela, com vida cheia
    public static int DISTANCIA_MINIMA_SPAWN = 400;

    public void renascer(int larguraJanela, int alturaJanela, int playerX, int playerY) {
        int maxX = Math.max(1, larguraJanela - LARGURA);
        int maxY = Math.max(1, alturaJanela - ALTURA);

        for (int tentativa = 0; tentativa < 30; tentativa++) {
            switch ((int) (Math.random() * 4)) {
                case 0:  x = 0;    y = (int) (Math.random() * maxY); break;
                case 1:  x = maxX; y = (int) (Math.random() * maxY); break;
                case 2:  y = 0;    x = (int) (Math.random() * maxX); break;
                default: y = maxY; x = (int) (Math.random() * maxX); break;
            }

            double dx = x - playerX;
            double dy = y - playerY;
            if (Math.sqrt(dx * dx + dy * dy) >= DISTANCIA_MINIMA_SPAWN) break;
        }
        pontosVida = 1;
        vida = true;
    }

    // Impede que o empurrão entre zumbis jogue alguém pra fora da tela
    public void limitar(int larguraJanela, int alturaJanela) {
        x = Math.max(0, Math.min(x, larguraJanela - LARGURA));
        y = Math.max(0, Math.min(y, alturaJanela - ALTURA));
    }
    // Ponto (o tiro) dentro da hitbox do zumbi?
    public boolean colideComPonto(int px, int py) {
        return px >= x && px <= x + LARGURA
            && py >= y && py <= y + ALTURA;
    }

    // Retângulo (o player) encostando na hitbox do zumbi?
    public boolean colideComRetangulo(int rx, int ry, int rLargura, int rAltura) {
        return x < rx + rLargura && x + LARGURA > rx
            && y < ry + rAltura && y + ALTURA > ry;
    }

    public void moverEmDirecao(int alvoX, int alvoY) {
        if (!vida) return;
        if (x < alvoX) x += velocidade;
        if (x > alvoX) x -= velocidade;
        if (y < alvoY) y += velocidade;
        if (y > alvoY) y -= velocidade;
    }

     public void setVelocidade(int v) {
        this.velocidade = v;
    }

    public void deslocar(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
}