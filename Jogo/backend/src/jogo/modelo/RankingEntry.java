package jogo.modelo;

import jogo.util.JsonObject;
import jogo.util.JsonSerializavel;

/** Uma linha do ranking: quem jogou, quantos zumbis matou e quanto tempo sobreviveu. */
public class RankingEntry implements JsonSerializavel {

    private final String nome;
    private final int kills;
    private final int tempoSegundos;

    public RankingEntry(String nome, int kills, int tempoSegundos) {
        this.nome = nome;
        this.kills = kills;
        this.tempoSegundos = tempoSegundos;
    }

    public String getNome() {
        return nome;
    }

    public int getKills() {
        return kills;
    }

    public int getTempoSegundos() {
        return tempoSegundos;
    }

    @Override
    public String toJson() {
        return JsonObject.novo()
                .campo("nome", nome)
                .campo("kills", kills)
                .campo("tempo", tempoSegundos)
                .construir();
    }
}
