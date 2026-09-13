import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private static Zombie zumbi = new Zombie(500, 300);
    private static Player player = new Player(100, 100);

    public static void main(String[] args) throws Exception {

    Database.criarTabelas();

    HttpServer server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        server.createContext("/", Main::pagina);

        server.createContext("/player", Main::player);

        server.start();
        
        Thread jogo = new Thread(() -> {

    while (true) {

        zumbi.moverEmDirecao(player.getX(), player.getY());

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            break;
        }
    }

});

jogo.start();

        System.out.println("ta funcion ando porra");
        System.out.println("http://localhost:8080");
    }

    private static void pagina(HttpExchange exchange) throws IOException {

        String caminho = exchange.getRequestURI().getPath();

        if (caminho.equals("/")) {
            caminho = "/index.html";
        }

        Path arquivo = Path.of(
                "frontend" + caminho
        );

        if (!Files.exists(arquivo)) {

            String resposta = "Arquivo não encontrado.";
            byte[] dadosResposta = resposta.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(
                    404,
                    dadosResposta.length
            );

            exchange.getResponseBody()
                    .write(dadosResposta);

            exchange.close();

            return;
        }

        byte[] dados = Files.readAllBytes(arquivo);

        String tipo = "text/plain";
        String caminhoLower = arquivo.toString().toLowerCase();

        if (caminhoLower.endsWith(".html")) {
            tipo = "text/html";
        }

        if (caminhoLower.endsWith(".css")) {
            tipo = "text/css";
        }

        if (caminhoLower.endsWith(".js")) {
            tipo = "application/javascript";
        }

        if (caminhoLower.endsWith(".png")) {
            tipo = "image/png";
        }

        if (caminhoLower.endsWith(".jpg") || caminhoLower.endsWith(".jpeg")) {
            tipo = "image/jpeg";
        }

        exchange.getResponseHeaders()
                .set("Content-Type", tipo);

        exchange.sendResponseHeaders(
                200,
                dados.length
        );

        exchange.getResponseBody()
                .write(dados);

        exchange.close();
    }

    private static void player(HttpExchange exchange)
            throws IOException {

        if (exchange.getRequestMethod().equals("POST")) {

            String comando = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim().toUpperCase();

            player.movimentacaoSurviver(comando);
        }

      String resposta =
        "{ \"x\": " + player.getX() +
        ", \"y\": " + player.getY() +
        ", \"zumbiX\": " + (int) zumbi.getX() +
        ", \"zumbiY\": " + (int) zumbi.getY() +
        ", \"pontosVida\": " + (int) player.getPontosVida() +
        " }";

        byte[] dados = resposta.getBytes(StandardCharsets.UTF_8);
                         
        exchange.getResponseHeaders()
                .set("Content-Type", "application/json");

        exchange.sendResponseHeaders(
                200,
                dados.length
        );

        exchange.getResponseBody()
                .write(dados);

        exchange.close();
    }
}