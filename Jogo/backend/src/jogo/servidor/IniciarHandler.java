package jogo.servidor;

import jogo.banco.BancoException;
import com.sun.net.httpserver.HttpExchange;
import jogo.logica.Jogo;


/** POST /iniciar  corpo: nome do jogador — começa a partida. */
public class IniciarHandler extends BaseHandler {

    private final Jogo jogo;

    public IniciarHandler(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void tratar(HttpExchange exchange) throws Exception {
        try {
            jogo.iniciar(lerCorpo(exchange));
            responderJson(exchange, "{\"ok\":true}");

        } catch (BancoException e) {
            e.printStackTrace();
            responderJson(exchange, "{\"ok\":false}");
        }
    }
}
