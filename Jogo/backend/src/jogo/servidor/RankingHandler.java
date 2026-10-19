package jogo.servidor;

import jogo.banco.BancoException;
import com.sun.net.httpserver.HttpExchange;
import jogo.banco.PartidaRepository;
import jogo.util.Json;

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

        } catch (BancoException e) {
            e.printStackTrace();
            responderJson(exchange, "[]");
        }
    }
}
