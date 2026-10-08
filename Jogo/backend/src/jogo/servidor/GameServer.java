package jogo.servidor;

import com.sun.net.httpserver.HttpServer;
import jogo.banco.PartidaRepository;
import jogo.logica.Jogo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

/** Monta o servidor HTTP e liga cada rota ao seu handler. */
public class GameServer {

    private final HttpServer servidor;

    public GameServer(int porta, Jogo jogo, PartidaRepository partidas,
                      Path pastaFrontend) throws IOException {

        servidor = HttpServer.create(new InetSocketAddress(porta), 0);

        servidor.createContext("/", new ArquivoEstaticoHandler(pastaFrontend));
        servidor.createContext("/player", new PlayerHandler(jogo));
        servidor.createContext("/tiro", new TiroHandler(jogo));
        servidor.createContext("/recarregar", new RecarregarHandler(jogo));
        servidor.createContext("/reiniciar", new ReiniciarHandler(jogo));
        servidor.createContext("/iniciar", new IniciarHandler(jogo));
        servidor.createContext("/parar", new PararHandler(jogo));
        servidor.createContext("/ranking", new RankingHandler(partidas));
    }

    public void iniciar() {
        servidor.start();
    }
}
