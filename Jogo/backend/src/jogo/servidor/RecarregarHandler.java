package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;
import jogo.logica.Jogo;
import jogo.util.JsonObject;

/** POST /recarregar — começa a recarregar a arma (tecla R). */
public class RecarregarHandler extends BaseHandler {

    private final Jogo jogo;

    public RecarregarHandler(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void tratar(HttpExchange exchange) throws Exception {
        boolean iniciou = isPost(exchange) && jogo.recarregar();

        String resposta = JsonObject.novo()
                .campo("recarregando", iniciou)
                .construir();

        responderJson(exchange, resposta);
    }
}
