package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;
import jogo.logica.Jogo;

/** POST /parar — encerra a partida, salva no banco e devolve kills e tempo. */
public class PararHandler extends BaseHandler {

    private final Jogo jogo;

    public PararHandler(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void tratar(HttpExchange exchange) throws Exception {
        responderJson(exchange, jogo.parar().toJson());
    }
}
