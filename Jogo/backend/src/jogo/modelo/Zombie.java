package jogo.modelo;

import jogo.util.JsonObject;
import jogo.util.JsonSerializavel;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Zombie extends Personagem implements JsonSerializavel {

    // Tamanho da hitbox. Ajuste pra bater com .zumbi no style.css
    public static final int LARGURA = 57;
    public static final int ALTURA = 95;

    /** Velocidade em pixels por SEGUNDO. */
    private static final int VELOCIDADE_PADRAO = 150;
    private static final int VIDA_INICIAL = 1;
    private static final int DISTANCIA_MINIMA_SPAWN = 400;

    /** Quão rápido o zumbi consegue mudar de direção (maior = mais ágil). */
    private static final double AGILIDADE = 5.0;

    /** Acima disso o zumbi começa a "contornar" o player em vez de ir em linha reta. */
    private static final double DISTANCIA_FLANCO = 140;
    private static final double ALCANCE_FLANCO = 380;
    private static final double ANGULO_FLANCO_MAX = 0.55; // radianos (~31 graus)

    private static final AtomicInteger PROXIMO_ID = new AtomicInteger(1);

    private final int id = PROXIMO_ID.getAndIncrement();

    // Personalidade: cada zumbi anda de um jeito um pouco diferente
    private final double fase = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
    private final double frequenciaArrasto = 1.2 + ThreadLocalRandom.current().nextDouble() * 1.6;
    private final double ladoDoFlanco = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
    private final double forcaDoFlanco = 0.4 + ThreadLocalRandom.current().nextDouble() * 0.6;

    private double vx;
    private double vy;

    public Zombie(int x, int y) {
        super(x, y, LARGURA, ALTURA, VELOCIDADE_PADRAO, VIDA_INICIAL);
    }

    public int getId() {
        return id;
    }

    /** Coloca o zumbi num ponto (dentro da tela), parado e com vida cheia. */
    public void posicionar(double novoX, double novoY, Tela tela) {
        this.x = novoX;
        this.y = novoY;
        this.vx = 0;
        this.vy = 0;
        limitarA(tela);
        restaurarVida(VIDA_INICIAL);
    }

    /** Nasce numa borda da tela, longe do alvo, com vida cheia. */
    public void renascer(Tela tela, Player alvo) {
        int maxX = Math.max(1, tela.getLargura() - LARGURA);
        int maxY = Math.max(1, tela.getAltura() - ALTURA);
        Random sorteio = ThreadLocalRandom.current();

        for (int tentativa = 0; tentativa < 30; tentativa++) {
            switch (sorteio.nextInt(4)) {
                case 0:  x = 0;    y = sorteio.nextInt(maxY); break;
                case 1:  x = maxX; y = sorteio.nextInt(maxY); break;
                case 2:  y = 0;    x = sorteio.nextInt(maxX); break;
                default: y = maxY; x = sorteio.nextInt(maxX); break;
            }

            double dx = x - alvo.getXExato();
            double dy = y - alvo.getYExato();

            if (Math.sqrt(dx * dx + dy * dy) >= DISTANCIA_MINIMA_SPAWN) {
                break;
            }
        }

        vx = 0;
        vy = 0;
        restaurarVida(VIDA_INICIAL);
    }

    /**
     * Um passo de perseguição.
     *
     * @param alvoX   centro do player
     * @param alvoY   centro do player
     * @param sepX    empurrão (px/s) para não ficar em cima dos outros zumbis
     * @param sepY    idem
     * @param agora   relógio em ms, usado só para o ritmo "arrastado" da caminhada
     */
    public void perseguir(double dt, double alvoX, double alvoY,
                          double sepX, double sepY, long agora) {
        if (!isVivo()) {
            return;
        }

        double dx = alvoX - getCentroX();
        double dy = alvoY - getCentroY();
        double distancia = Math.sqrt(dx * dx + dy * dy);

        double dirX = 0;
        double dirY = 0;

        if (distancia > 1) {
            dirX = dx / distancia;
            dirY = dy / distancia;

            // Longe do player, a rota faz uma curva (cada zumbi para um lado).
            // Perto, o desvio some e todos vão direto em cima dele.
            double longe = clamp((distancia - DISTANCIA_FLANCO) / ALCANCE_FLANCO, 0, 1);
            double angulo = ladoDoFlanco * forcaDoFlanco * ANGULO_FLANCO_MAX * longe;
            double cos = Math.cos(angulo);
            double sen = Math.sin(angulo);
            double rx = dirX * cos - dirY * sen;
            double ry = dirX * sen + dirY * cos;
            dirX = rx;
            dirY = ry;
        }

        // Anda "aos trancos": acelera e desacelera um pouco, cada um no seu ritmo
        double ritmo = 1 + 0.18 * Math.sin(agora / 1000.0 * frequenciaArrasto * Math.PI + fase);

        // Quando já está em cima do alvo, vai parando (evita tremer no mesmo lugar)
        double chegada = clamp(distancia / 14.0, 0, 1);

        double velocidade = getVelocidade() * ritmo * chegada;

        double desejadoX = dirX * velocidade + sepX;
        double desejadoY = dirY * velocidade + sepY;

        // limite de velocidade, para a separação não deixar ninguém "turbinado"
        double maximo = getVelocidade() * 1.3;
        double modulo = Math.sqrt(desejadoX * desejadoX + desejadoY * desejadoY);
        if (modulo > maximo) {
            desejadoX = desejadoX / modulo * maximo;
            desejadoY = desejadoY / modulo * maximo;
        }

        // A velocidade vai ENCOSTANDO na desejada (inércia): curvas e arrancadas suaves
        double suavizacao = 1 - Math.exp(-AGILIDADE * dt);
        vx += (desejadoX - vx) * suavizacao;
        vy += (desejadoY - vy) * suavizacao;

        deslocar(vx * dt, vy * dt);
    }

    /** Além de segurar o zumbi dentro da tela, zera a velocidade que o empurraria para fora. */
    @Override
    public void limitarA(Tela tela) {
        super.limitarA(tela);

        if ((x <= 0 && vx < 0) || (x >= tela.getLargura() - LARGURA && vx > 0)) {
            vx = 0;
        }
        if ((y <= 0 && vy < 0) || (y >= tela.getAltura() - ALTURA && vy > 0)) {
            vy = 0;
        }
    }

    private static double clamp(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }

    @Override
    public String toJson() {
        return JsonObject.novo()
                .campo("id", id)
                .campo("x", x)
                .campo("y", y)
                .campo("vx", (int) Math.round(vx))
                .campo("vy", (int) Math.round(vy))
                .construir();
    }
}
