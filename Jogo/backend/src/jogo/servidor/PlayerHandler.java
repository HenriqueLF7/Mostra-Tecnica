package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;
import jogo.logica.Jogo;
import jogo.modelo.Movimento;
import jogo.modelo.Tela;

/**
 * /player
 *   GET  -> devolve o estado do jogo
 *   POST -> corpo "teclas;largura;altura" avisa quais teclas estão apertadas
 *           (ex.: "WD" = cima + direita, "-" = nenhuma) e devolve o estado.
 *           Só é preciso mandar quando as teclas MUDAM (e de tempos em tempos,
 *           para o servidor saber que o front continua vivo).
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

            int dx = 0;
            int dy = 0;

            for (char tecla : partes[0].toCharArray()) {
                Movimento movimento = Movimento.deComando(String.valueOf(tecla));

                if (movimento != null) {
                    dx += movimento.getDx();
                    dy += movimento.getDy();
                }
            }

            Tela tela = new Tela(Integer.parseInt(partes[1]), Integer.parseInt(partes[2]));

            jogo.definirDirecaoPlayer(dx, dy, tela);
        }

        responderJson(exchange, jogo.toJson());
    }
}
