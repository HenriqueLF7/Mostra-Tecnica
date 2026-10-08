package jogo.logica;

import jogo.modelo.Player;
import jogo.modelo.Tela;
import jogo.modelo.Zombie;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * Decide quando uma nova onda nasce e quantos zumbis vêm nela.
 *
 * Os zumbis da onda não aparecem todos de uma vez: chegam em "bandos"
 * (grupinhos vindos do mesmo ponto da borda), um depois do outro. Isso
 * deixa a horda mais viva e evita aquela parede de zumbis no mesmo instante.
 */
public class GerenciadorDeOndas {

    private static final long PAUSA_ENTRE_ONDAS_MS = 1000;
    private static final int MAX_ZUMBIS_POR_ONDA = 24;
    private static final int DISTANCIA_MINIMA_SPAWN = 400;

    private static final int VELOCIDADE_BASE = 150;      // px/s na primeira onda
    private static final int ACRESCIMO_VELOCIDADE = 25;  // a cada 2 ondas (até +2 vezes)

    private static final int TAMANHO_MAX_DO_BANDO = 5;
    private static final long INTERVALO_BASE_MS = 1500;
    private static final long INTERVALO_MIN_MS = 500;

    private final Deque<Bando> fila = new ArrayDeque<>();
    private final Random sorteio = new Random();

    private int onda = 0;
    private long momentoOndaLimpa = 0;

    /** Um grupinho de zumbis que nasce junto, num horário combinado. */
    private static final class Bando {
        final long momento;
        final int quantidade;
        final int velocidadeBase;

        Bando(long momento, int quantidade, int velocidadeBase) {
            this.momento = momento;
            this.quantidade = quantidade;
            this.velocidadeBase = velocidadeBase;
        }
    }

    public int getOnda() {
        return onda;
    }

    public void reiniciar() {
        onda = 0;
        momentoOndaLimpa = 0;
        fila.clear();
    }

    public void atualizar(Horda horda, Player player, Tela tela, long agora) {
        liberarBandos(horda, player, tela, agora);

        // a onda só acaba quando todos os bandos já nasceram E morreram
        if (!horda.isVazia() || !fila.isEmpty()) {
            momentoOndaLimpa = 0;
            return;
        }

        if (momentoOndaLimpa == 0) {
            momentoOndaLimpa = agora;
        } else if (agora - momentoOndaLimpa >= PAUSA_ENTRE_ONDAS_MS) {
            planejarOnda(agora);
            momentoOndaLimpa = 0;
            liberarBandos(horda, player, tela, agora);
        }
    }

    /** Divide a onda em bandos e marca a hora em que cada um nasce. */
    private void planejarOnda(long agora) {
        onda++;

        int total = (int) Math.min(MAX_ZUMBIS_POR_ONDA, Math.pow(2, onda - 1));
        int velocidadeBase = VELOCIDADE_BASE + ACRESCIMO_VELOCIDADE * Math.min((onda - 1) / 2, 2);

        int tamanhoMax = Math.min(TAMANHO_MAX_DO_BANDO, 1 + onda / 2);
        double intervaloMedio = Math.max(INTERVALO_MIN_MS, INTERVALO_BASE_MS - 100L * onda);

        long momento = agora;
        int restante = total;

        while (restante > 0) {
            int tamanho = Math.min(restante, 1 + sorteio.nextInt(tamanhoMax));
            fila.add(new Bando(momento, tamanho, velocidadeBase));

            restante -= tamanho;
            momento += (long) (intervaloMedio * (0.6 + sorteio.nextDouble() * 0.8));
        }
    }

    private void liberarBandos(Horda horda, Player player, Tela tela, long agora) {
        while (!fila.isEmpty() && fila.peek().momento <= agora) {
            spawnarBando(fila.poll(), horda, player, tela);
        }
    }

    private void spawnarBando(Bando bando, Horda horda, Player player, Tela tela) {
        int maxX = Math.max(1, tela.getLargura() - Zombie.LARGURA);
        int maxY = Math.max(1, tela.getAltura() - Zombie.ALTURA);

        // Sorteia um ponto da borda longe do player (se não achar, fica com o mais distante)
        int borda = 0;
        double pontoX = 0;
        double pontoY = 0;
        double maiorDistancia = -1;

        for (int tentativa = 0; tentativa < 20; tentativa++) {
            int bordaSorteada = sorteio.nextInt(4);
            double px;
            double py;

            switch (bordaSorteada) {
                case 0:  px = 0;    py = sorteio.nextInt(maxY); break;
                case 1:  px = maxX; py = sorteio.nextInt(maxY); break;
                case 2:  py = 0;    px = sorteio.nextInt(maxX); break;
                default: py = maxY; px = sorteio.nextInt(maxX); break;
            }

            double dx = px - player.getXExato();
            double dy = py - player.getYExato();
            double distancia = Math.sqrt(dx * dx + dy * dy);

            if (distancia > maiorDistancia) {
                maiorDistancia = distancia;
                borda = bordaSorteada;
                pontoX = px;
                pontoY = py;
            }

            if (distancia >= DISTANCIA_MINIMA_SPAWN) {
                break;
            }
        }

        // Os zumbis do bando nascem espalhados perto desse ponto
        for (int i = 0; i < bando.quantidade; i++) {
            Zombie zumbi = new Zombie(0, 0);

            // cada um com a sua velocidade (de 85% a 115% da base)
            zumbi.setVelocidade((int) Math.round(bando.velocidadeBase * (0.85 + sorteio.nextDouble() * 0.30)));

            double aoLongoDaBorda = (sorteio.nextDouble() - 0.5) * 160;
            double paraDentro = sorteio.nextDouble() * 50;

            double x;
            double y;

            switch (borda) {
                case 0:  x = paraDentro;        y = pontoY + aoLongoDaBorda; break;
                case 1:  x = maxX - paraDentro; y = pontoY + aoLongoDaBorda; break;
                case 2:  y = paraDentro;        x = pontoX + aoLongoDaBorda; break;
                default: y = maxY - paraDentro; x = pontoX + aoLongoDaBorda; break;
            }

            zumbi.posicionar(x, y, tela);
            horda.adicionar(zumbi);
        }
    }
}
