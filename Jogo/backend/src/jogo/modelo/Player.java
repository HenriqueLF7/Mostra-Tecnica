package jogo.modelo;

public class Player extends Personagem {

    public static final int LARGURA = 90;
    public static final int ALTURA = 100;

    /** Velocidade em pixels por SEGUNDO (não depende mais da taxa de frames). */
    private static final int VELOCIDADE = 300;
    private static final int VIDA_INICIAL = 3;
    private static final long INVULNERABILIDADE_MS = 1000;

    /** Se o front parar de avisar (aba travada, queda de rede), o player para sozinho. */
    private static final long ENTRADA_EXPIRA_MS = 1000;

    // direção desejada: -1, 0 ou 1 em cada eixo (vem das teclas W A S D)
    private int dirX;
    private int dirY;
    private long ultimaEntrada;

    // velocidade atual em px/s (o front usa para suavizar o desenho)
    private double vx;
    private double vy;

    private long invulneravelAte;

    public Player(int x, int y) {
        super(x, y, LARGURA, ALTURA, VELOCIDADE, VIDA_INICIAL);
    }

    /** Cria o player no centro da tela. */
    public static Player noCentro(Tela tela) {
        return new Player(
                (tela.getLargura() - LARGURA) / 2,
                (tela.getAltura() - ALTURA) / 2
        );
    }

    /** Guarda para onde as teclas estão mandando o player andar (-1, 0 ou 1 por eixo). */
    public void definirDirecao(int dx, int dy, long agora) {
        this.dirX = Integer.signum(dx);
        this.dirY = Integer.signum(dy);
        this.ultimaEntrada = agora;
    }

    /** Anda continuamente na direção pedida, a cada frame do servidor. */
    public void atualizar(double dt, long agora, Tela tela) {
        if (!isVivo()) {
            vx = 0;
            vy = 0;
            return;
        }

        if (agora - ultimaEntrada > ENTRADA_EXPIRA_MS) {
            dirX = 0;
            dirY = 0;
        }

        // na diagonal divide por raiz de 2, senão o player seria 41% mais rápido
        double tamanho = Math.sqrt(dirX * dirX + dirY * dirY);
        vx = tamanho == 0 ? 0 : dirX / tamanho * getVelocidade();
        vy = tamanho == 0 ? 0 : dirY / tamanho * getVelocidade();

        deslocar(vx * dt, vy * dt);
        limitarA(tela);

        // encostou na parede: o front não deve continuar "empurrando" o desenho
        if ((x <= 0 && vx < 0) || (x >= tela.getLargura() - LARGURA && vx > 0)) {
            vx = 0;
        }
        if ((y <= 0 && vy < 0) || (y >= tela.getAltura() - ALTURA && vy > 0)) {
            vy = 0;
        }
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    /**
     * Tenta tirar vida do player. Só funciona se já passou o tempo de
     * invulnerabilidade desde o último dano.
     *
     * @return true se o dano foi aplicado
     */
    public boolean tentarReceberDano(int dano, long agora) {
        if (agora < invulneravelAte) {
            return false;
        }

        receberDano(dano);
        invulneravelAte = agora + INVULNERABILIDADE_MS;
        return true;
    }

    /** Está no período de proteção depois de levar dano? (o front pisca o personagem) */
    public boolean isInvulneravel(long agora) {
        return isVivo() && agora < invulneravelAte;
    }
}
