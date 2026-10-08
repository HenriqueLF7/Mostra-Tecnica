package jogo.logica;

import jogo.modelo.Player;
import jogo.modelo.Tela;
import jogo.modelo.Zombie;
import jogo.util.Json;
import jogo.util.JsonSerializavel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** O grupo de zumbis que estão na tela. */
public class Horda implements JsonSerializavel {

    private static final int DISTANCIA_ENTRE_ZUMBIS = 80;
    private static final int EMPURRAO_MAX = 8;

    private final List<Zombie> zumbis = new ArrayList<>();

    public void adicionar(Zombie zumbi) {
        zumbis.add(zumbi);
    }

    public void limpar() {
        zumbis.clear();
    }

    public boolean isVazia() {
        return zumbis.isEmpty();
    }

    public List<Zombie> getZumbis() {
        return Collections.unmodifiableList(zumbis);
    }

    public void removerMortos() {
        zumbis.removeIf(z -> !z.isVivo());
    }

    /** Todos os zumbis dão um passo na direção do player. */
    public void perseguir(Player player) {
        for (Zombie z : zumbis) {
            z.moverEmDirecao(player.getX(), player.getY());
        }
    }

    /** Empurra os zumbis que estão perto demais uns dos outros. */
    public void separar(Tela tela) {
        for (int i = 0; i < zumbis.size(); i++) {
            for (int j = i + 1; j < zumbis.size(); j++) {
                afastarSeMuitoPerto(zumbis.get(i), zumbis.get(j), i, j);
            }
        }

        for (Zombie z : zumbis) {
            z.limitarA(tela);
        }
    }

    private void afastarSeMuitoPerto(Zombie a, Zombie b, int i, int j) {
        double dx = (a.getX() + Zombie.LARGURA / 2.0) - (b.getX() + Zombie.LARGURA / 2.0);
        double dy = (a.getY() + Zombie.ALTURA / 2.0) - (b.getY() + Zombie.ALTURA / 2.0);
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist >= DISTANCIA_ENTRE_ZUMBIS) {
            return;
        }

        if (dist < 1) {
            // exatamente no mesmo ponto: escolhe uma direção qualquer
            double angulo = i * 31 + j * 17;
            dx = Math.cos(angulo);
            dy = Math.sin(angulo);
            dist = 1;
        }

        double empurrao = Math.min(EMPURRAO_MAX, (DISTANCIA_ENTRE_ZUMBIS - dist) / 2);
        int mx = (int) Math.round(dx / dist * empurrao);
        int my = (int) Math.round(dy / dist * empurrao);

        a.deslocar(mx, my);
        b.deslocar(-mx, -my);
    }

    @Override
    public String toJson() {
        return Json.array(zumbis);
    }
}
