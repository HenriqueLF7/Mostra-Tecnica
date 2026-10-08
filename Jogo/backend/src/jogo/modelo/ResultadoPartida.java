package jogo.modelo;

import jogo.util.JsonObject;
import jogo.util.JsonSerializavel;

/** Resumo de uma partida, mostrado na tela de Game Over. */
public class ResultadoPartida implements JsonSerializavel {

    private final int kills;
    private final long tempoSegundos;

    public ResultadoPartida(int kills, long tempoSegundos) {
        this.kills = kills;
        this.tempoSegundos = tempoSegundos;
    }

    public int getKills() {
        return kills;
    }

    public long getTempoSegundos() {
        return tempoSegundos;
    }

    @Override
    public String toJson() {
        return JsonObject.novo()
                .campo("kills", kills)
                .campo("tempo", tempoSegundos)
                .construir();
    }
}
