package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;
import jogo.banco.PartidaRepository;
import jogo.util.Json;

import java.sql.SQLException;
import java.util.List;

/** GET /ranking — top 5 partidas. */
public class RankingHandler extends BaseHandler {

    private final PartidaRepository partidas;

    public RankingHandler(PartidaRepository partidas) {
        this.partidas = partidas;
    }

    @Override
    protected void tratar(HttpExchange exchange) throws Exception {
        try {
            responderJson(exchange, Json.array(partidas.buscarRanking()));

        } catch (SQLException e) {
            e.printStackTrace();
            responderJson(exchange, "[]");
        }
    }
}
