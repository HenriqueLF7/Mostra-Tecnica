public class Tiro {

    public static int MUZZLE_OFFSET_X = 86;
    public static int MUZZLE_OFFSET_Y = 52;
    public static int LARGURA_PLAYER = 90;

    private static final double VELOCIDADE_PADRAO = 50;

    private double x;
    private double y;
    private double velocidade;
    private String direcao;   // lado que o boneco olha: "esquerda" ou "direita"
    private double dirX;      // vetor normalizado
    private double dirY;
    private double angulo;    // em graus, pro front girar o sprite do tiro
    private boolean ativo = true;

    private static final int DANO_PADRAO = 1;
    private int dano = DANO_PADRAO;
    public int getDano() { return dano; }

    public Tiro(double xInicial, double yInicial, double alvoX, double alvoY, String direcao) {
        this.x = xInicial;
        this.y = yInicial;
        this.direcao = direcao;
        this.velocidade = VELOCIDADE_PADRAO;

        double dx = alvoX - xInicial;
        double dy = alvoY - yInicial;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < 1) {
            // mouse exatamente na ponta da arma: atira pro lado que ele olha
            this.dirX = "esquerda".equals(direcao) ? -1 : 1;
            this.dirY = 0;
        } else {
            this.dirX = dx / dist;
            this.dirY = dy / dist;
        }

        this.angulo = Math.toDegrees(Math.atan2(this.dirY, this.dirX));
    }

    // Nasce na ponta da arma e voa em direção ao alvo (mouse)
    public static Tiro criarNaPontaDaArma(int playerX, int playerY,
                                          int alvoX, int alvoY, String direcao) {

        int offsetX = "esquerda".equals(direcao)
                ? (LARGURA_PLAYER - MUZZLE_OFFSET_X)
                : MUZZLE_OFFSET_X;

        int nascX = playerX + offsetX;
        int nascY = playerY + MUZZLE_OFFSET_Y;

        return new Tiro(nascX, nascY, alvoX, alvoY, direcao);
    }

    public void mover() {
        if (!ativo) return;
        x += dirX * velocidade;
        y += dirY * velocidade;
    }

    public boolean saiuDaTela(int larguraJanela, int alturaJanela) {
        boolean fora = x < 0 || x > larguraJanela || y < 0 || y > alturaJanela;
        if (fora) ativo = false;
        return fora;
    }

    public String toJson() {
        return "{ \"x\": " + (int) x +
                ", \"y\": " + (int) y +
                ", \"angulo\": " + (int) angulo +
                ", \"direcao\": \"" + direcao + "\"" +
                " }";
    }

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public String getDirecao() { return direcao; }
    public boolean isAtivo() { return ativo; }
}