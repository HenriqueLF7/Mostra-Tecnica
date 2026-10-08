package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;
import jogo.logica.Jogo;
import jogo.modelo.Lado;
import jogo.modelo.Tela;
import jogo.util.JsonObject;

/** POST /tiro  corpo: "direcao;largura;altura;mouseX;mouseY" (os 4 últimos são opcionais). */
public class TiroHandler extends BaseHandler {

    private final Jogo jogo;

    public TiroHandler(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void tratar(HttpExchange exchange) throws Exception {
        boolean disparou = false;

        if (isPost(exchange)) {
            String[] partes = lerCorpo(exchange).split(";");

            Lado lado = Lado.deTexto(partes[0]);
            Tela tela = partes.length > 2
                    ? new Tela(Integer.parseInt(partes[1]), Integer.parseInt(partes[2]))
                    : null;

            if (partes.length > 4) {
                disparou = jogo.atirar(lado, tela,
                        Integer.parseInt(partes[3]), Integer.parseInt(partes[4]));
            } else {
                disparou = jogo.atirar(lado, tela);
            }
        }

        String resposta = JsonObject.novo()
                .campo("disparou", disparou)
                .campo("cooldownRestante", jogo.getCooldownRestante())
                .campoJson("tiros", jogo.getTirosJson())
                .construir();

        responderJson(exchange, resposta);
    }
}
