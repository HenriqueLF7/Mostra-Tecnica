package jogo.banco;

import jogo.modelo.RankingEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Acesso à tabela "partidas" no Supabase e ao ranking global. */
public class PartidaRepository {

    private static final int TAMANHO_RANKING = 5;

    private final ConexaoBanco banco;

    public PartidaRepository(ConexaoBanco banco) {
        this.banco = banco;
    }

    public void salvar(int jogadorId, int kills, int tempoSegundos) throws BancoException {
        String corpo = "{\"jogador_id\": " + jogadorId
                + ", \"kills\": " + kills
                + ", \"tempo_segundos\": " + tempoSegundos + "}";

        banco.post("/rest/v1/partidas", corpo, "return=minimal");
    }

    /** As melhores partidas: mais kills primeiro; empate decide pelo maior tempo. */
    public List<RankingEntry> buscarRanking() throws BancoException {
        Object resp = banco.get("/rest/v1/partidas"
                + "?select=kills,tempo_segundos,jogadores(nome)"
                + "&order=kills.desc,tempo_segundos.desc"
                + "&limit=" + TAMANHO_RANKING);

        List<RankingEntry> ranking = new ArrayList<>();

        if (resp instanceof List<?> linhas) {
            for (Object o : linhas) {
                if (!(o instanceof Map<?, ?> linha)) continue;

                String nome = "?";
                if (linha.get("jogadores") instanceof Map<?, ?> j && j.get("nome") != null) {
                    nome = j.get("nome").toString();
                }
                ranking.add(new RankingEntry(
                        nome,
                        ((Number) linha.get("kills")).intValue(),
                        ((Number) linha.get("tempo_segundos")).intValue()));
            }
        }
        return ranking;
    }
}
