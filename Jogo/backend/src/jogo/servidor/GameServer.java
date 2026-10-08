package jogo.servidor;

import com.sun.net.httpserver.HttpServer;
import jogo.banco.PartidaRepository;
import jogo.logica.Jogo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.Executors;

/** Monta o servidor HTTP e liga cada rota ao seu handler. */
public class GameServer {

    private final HttpServer servidor;

    public GameServer(int porta, Jogo jogo, PartidaRepository partidas,
                      Path pastaFrontend) throws IOException {

        // Desliga o algoritmo de Nagle (TCP_NODELAY). Sem isso cada resposta demora ~40 ms
        // para chegar no navegador (cabeçalho e corpo saem em pacotes separados) e o jogo
        // inteiro fica "travando". Precisa ser definido ANTES de criar o servidor.
        System.setProperty("sun.net.httpserver.nodelay", "true");

        servidor = HttpServer.create(new InetSocketAddress(porta), 0);

        // Sem isso o servidor atende UMA requisição por vez: o polling do estado,
        // as teclas e os tiros ficavam na mesma fila. Com várias threads, não esperam um ao outro.
        servidor.setExecutor(Executors.newFixedThreadPool(8));

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
