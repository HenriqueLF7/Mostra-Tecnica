package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;
import jogo.logica.Jogo;
import jogo.modelo.Tela;

/** POST /reiniciar  corpo: "largura;altura" — zera o jogo e deixa pausado. */
public class ReiniciarHandler extends BaseHandler {

    private final Jogo jogo;

    public ReiniciarHandler(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void tratar(HttpExchange exchange) throws Exception {
        Tela tela = null;

        if (isPost(exchange)) {
            String[] partes = lerCorpo(exchange).split(";");

            if (partes.length > 1) {
                tela = new Tela(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
            }
        }

        jogo.reiniciar(tela);

        responderJson(exchange, "{}");
    }
}
