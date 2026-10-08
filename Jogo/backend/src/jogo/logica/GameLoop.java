package jogo.logica;

import java.util.concurrent.locks.LockSupport;

/**
 * Roda o jogo continuamente (~60 vezes por segundo) numa thread própria.
 *
 * Cada frame recebe o tempo real que passou desde o anterior (dt). Assim a
 * velocidade dos personagens é sempre a mesma, mesmo que o computador
 * demore um pouquinho mais em algum frame.
 */
public class GameLoop implements Runnable {

    private static final long INTERVALO_NS = 1_000_000_000L / 60;

    /** Se o servidor engasgar (ex.: GC), não deixa o jogo "pular" um bloco enorme de movimento. */
    private static final double DT_MAXIMO = 0.05;

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
        long anterior = System.nanoTime();

        while (rodando) {
            long inicio = System.nanoTime();
            double dt = Math.min((inicio - anterior) / 1_000_000_000.0, DT_MAXIMO);
            anterior = inicio;

            try {
                jogo.atualizar(System.currentTimeMillis(), dt);
            } catch (RuntimeException e) {
                // um erro num frame não pode derrubar o jogo inteiro
                e.printStackTrace();
            }

            long espera = INTERVALO_NS - (System.nanoTime() - inicio);

            if (espera > 0) {
                LockSupport.parkNanos(espera);
            }

            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
    }
}
