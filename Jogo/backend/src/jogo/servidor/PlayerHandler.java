package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;
import jogo.logica.Jogo;
import jogo.modelo.Movimento;
import jogo.modelo.Tela;

/**
 * /player
 *   GET  -> devolve o estado do jogo
 *   POST -> corpo "comando;largura;altura" move o player e devolve o estado
 */
public class PlayerHandler extends BaseHandler {

    private final Jogo jogo;

    public PlayerHandler(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void tratar(HttpExchange exchange) throws Exception {
        if (isPost(exchange)) {
            String[] partes = lerCorpo(exchange).split(";");

            Movimento movimento = Movimento.deComando(partes[0]);
            Tela tela = new Tela(Integer.parseInt(partes[1]), Integer.parseInt(partes[2]));

            jogo.moverPlayer(movimento, tela);
        }

        responderJson(exchange, jogo.toJson());
    }
}
