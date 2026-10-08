package jogo.logica;

/** Roda o jogo continuamente, 20 vezes por segundo, numa thread própria. */
public class GameLoop implements Runnable {

    private static final long INTERVALO_MS = 50;

    private final Jogo jogo;
    private volatile boolean rodando = true;

    public GameLoop(Jogo jogo) {
        this.jogo = jogo;
    }

    public void parar() {
        rodando = false;
    }

    @Override
    public void run() {
        while (rodando) {
            try {
                jogo.atualizar(System.currentTimeMillis());
            } catch (RuntimeException e) {
                // um erro num frame não pode derrubar o jogo inteiro
                e.printStackTrace();
            }

            try {
                Thread.sleep(INTERVALO_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
