package jogo.modelo;

import jogo.util.JsonObject;
import jogo.util.JsonSerializavel;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Zombie extends Personagem implements JsonSerializavel {

    // Tamanho da hitbox. Ajuste pra bater com .zumbi no style.css
    public static final int LARGURA = 57;
    public static final int ALTURA = 95;

    private static final int VELOCIDADE_PADRAO = 10;
    private static final int VIDA_INICIAL = 1;
    private static final int DISTANCIA_MINIMA_SPAWN = 400;

    public Zombie(int x, int y) {
        super(x, y, LARGURA, ALTURA, VELOCIDADE_PADRAO, VIDA_INICIAL);
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

            double dx = x - alvo.getX();
            double dy = y - alvo.getY();

            if (Math.sqrt(dx * dx + dy * dy) >= DISTANCIA_MINIMA_SPAWN) {
                break;
            }
        }

        restaurarVida(VIDA_INICIAL);
    }

    public void moverEmDirecao(int alvoX, int alvoY) {
        if (!isVivo()) {
            return;
        }

        int velocidade = getVelocidade();

        if (x < alvoX) x += velocidade;
        if (x > alvoX) x -= velocidade;
        if (y < alvoY) y += velocidade;
        if (y > alvoY) y -= velocidade;
    }

    @Override
    public String toJson() {
        return JsonObject.novo()
                .campo("x", x)
                .campo("y", y)
                .construir();
    }
}
