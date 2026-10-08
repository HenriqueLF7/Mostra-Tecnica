package jogo.logica;

import jogo.modelo.Player;
import jogo.modelo.Tela;
import jogo.modelo.Zombie;

/** Decide quando uma nova onda de zumbis nasce e quantos vêm nela. */
public class GerenciadorDeOndas {

    private static final long PAUSA_ENTRE_ONDAS_MS = 1000;
    private static final int MAX_ZUMBIS_POR_ONDA = 24;
    private static final int DISTANCIA_MINIMA_ENTRE_SPAWNS = 110;

    private int onda = 0;
    private long momentoOndaLimpa = 0;

    public int getOnda() {
        return onda;
    }

    public void reiniciar() {
        onda = 0;
        momentoOndaLimpa = 0;
    }

    /** Onda limpa: espera um pouco e manda a próxima (o dobro de zumbis). */
    public void atualizar(Horda horda, Player player, Tela tela, long agora) {
        if (!horda.isVazia()) {
            return;
        }

        if (momentoOndaLimpa == 0) {
            momentoOndaLimpa = agora;
        } else if (agora - momentoOndaLimpa >= PAUSA_ENTRE_ONDAS_MS) {
            spawnarOnda(horda, player, tela);
            momentoOndaLimpa = 0;
        }
    }

    private void spawnarOnda(Horda horda, Player player, Tela tela) {
        onda++;

        int quantidade = (int) Math.min(MAX_ZUMBIS_POR_ONDA, Math.pow(2, onda - 1));
        int velocidadeBase = 6 + Math.min((onda - 1) / 2, 2);

        for (int i = 0; i < quantidade; i++) {
            Zombie zumbi = new Zombie(0, 0);

            // cada zumbi com uma velocidade um pouco diferente (-1, 0 ou +1)
            zumbi.setVelocidade(velocidadeBase + (int) (Math.random() * 3) - 1);

            posicionarLongeDosOutros(zumbi, horda, player, tela);
            horda.adicionar(zumbi);
        }
    }

    private void posicionarLongeDosOutros(Zombie zumbi, Horda horda,
                                          Player player, Tela tela) {
        for (int tentativa = 0; tentativa < 20; tentativa++) {
            zumbi.renascer(tela, player);

            if (estaLivre(zumbi, horda)) {
                return;
            }
        }
    }

    private boolean estaLivre(Zombie zumbi, Horda horda) {
        for (Zombie outro : horda.getZumbis()) {
            int dx = outro.getX() - zumbi.getX();
            int dy = outro.getY() - zumbi.getY();

            if (Math.sqrt(dx * dx + dy * dy) < DISTANCIA_MINIMA_ENTRE_SPAWNS) {
                return false;
            }
        }
        return true;
    }
}
