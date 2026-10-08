package jogo.modelo;

public class Player extends Personagem {

    public static final int LARGURA = 90;
    public static final int ALTURA = 100;

    private static final int VELOCIDADE = 15;
    private static final int VIDA_INICIAL = 3;
    private static final long INVULNERABILIDADE_MS = 1000;

    private long ultimoDano;

    public Player(int x, int y) {
        super(x, y, LARGURA, ALTURA, VELOCIDADE, VIDA_INICIAL);
        // nasce com 1s de proteção
        this.ultimoDano = System.currentTimeMillis();
    }

    /** Cria o player no centro da tela. */
    public static Player noCentro(Tela tela) {
        return new Player(
                (tela.getLargura() - LARGURA) / 2,
                (tela.getAltura() - ALTURA) / 2
        );
    }

    /** Anda uma "passada" na direção pedida, sem sair da janela. */
    public void mover(Movimento movimento, Tela tela) {
        if (!isVivo()) {
            return;
        }

        if (movimento != null) {
            deslocar(movimento.getDx() * getVelocidade(),
                     movimento.getDy() * getVelocidade());
        }

        limitarA(tela);
    }

    /**
     * Tenta tirar vida do player. Só funciona se já passou o tempo de
     * invulnerabilidade desde o último dano.
     *
     * @return true se o dano foi aplicado
     */
    public boolean tentarReceberDano(int dano, long agora) {
        if (agora - ultimoDano < INVULNERABILIDADE_MS) {
            return false;
        }

        receberDano(dano);
        ultimoDano = agora;
        return true;
    }
}
