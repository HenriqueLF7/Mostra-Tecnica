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

    /** Dentro dessa distância (entre centros) os zumbis se afastam uns dos outros. */
    private static final double RAIO_SEPARACAO = 72;

    /** Força máxima do afastamento, em px/s (quando estão colados). */
    private static final double FORCA_SEPARACAO = 170;

    /** Zumbi é mais alto que largo: a distância vertical "pesa" menos. */
    private static final double PESO_VERTICAL = 0.7;

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

    /**
     * Um frame da horda: cada zumbi vai atrás do player e, ao mesmo tempo,
     * é empurrado para longe dos vizinhos. Os dois efeitos viram uma
     * única velocidade suave (nada de "teleportes" de separação).
     */
    public void atualizar(double dt, Player player, Tela tela, long agora) {
        int total = zumbis.size();
        if (total == 0) {
            return;
        }

        double[] sepX = new double[total];
        double[] sepY = new double[total];

        for (int i = 0; i < total; i++) {
            for (int j = i + 1; j < total; j++) {
                calcularSeparacao(i, j, sepX, sepY);
            }
        }

        double alvoX = player.getCentroX();
        double alvoY = player.getCentroY();

        for (int i = 0; i < total; i++) {
            Zombie zumbi = zumbis.get(i);

            zumbi.perseguir(dt, alvoX, alvoY, sepX[i], sepY[i], agora);
            zumbi.limitarA(tela);
        }
    }

    private void calcularSeparacao(int i, int j, double[] sepX, double[] sepY) {
        Zombie a = zumbis.get(i);
        Zombie b = zumbis.get(j);

        double dx = a.getCentroX() - b.getCentroX();
        double dy = (a.getCentroY() - b.getCentroY()) * PESO_VERTICAL;
        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia >= RAIO_SEPARACAO) {
            return;
        }

        if (distancia < 0.5) {
            // exatamente no mesmo ponto: escolhe uma direção qualquer (mas sempre a mesma para o par)
            double angulo = a.getId() * 31 + b.getId() * 17;
            dx = Math.cos(angulo);
            dy = Math.sin(angulo);
            distancia = 1;
        }

        // quanto mais perto, mais forte o empurrão
        double forca = (RAIO_SEPARACAO - distancia) / RAIO_SEPARACAO * FORCA_SEPARACAO;
        double fx = dx / distancia * forca;
        double fy = dy / distancia * forca;

        sepX[i] += fx;
        sepY[i] += fy;
        sepX[j] -= fx;
        sepY[j] -= fy;
    }

    @Override
    public String toJson() {
        return Json.array(zumbis);
    }
}
