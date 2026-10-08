package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Base de todos os handlers. Cuida do que se repete: ler o corpo da
 * requisição, escrever a resposta e tratar erros. Cada subclasse só
 * implementa {@link #tratar(HttpExchange)}.
 */
public abstract class BaseHandler implements HttpHandler {

    protected static final String JSON = "application/json; charset=UTF-8";

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        try {
            tratar(exchange);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            responderTexto(exchange, 400, "Requisição inválida.");
        } catch (Exception e) {
            e.printStackTrace();
            responderTexto(exchange, 500, "Erro interno.");
        } finally {
            exchange.close();
        }
    }

    protected abstract void tratar(HttpExchange exchange) throws Exception;

    protected String lerCorpo(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    protected boolean isPost(HttpExchange exchange) {
        return "POST".equals(exchange.getRequestMethod());
    }

    protected void responderJson(HttpExchange exchange, String json) throws IOException {
        responder(exchange, 200, JSON, json.getBytes(StandardCharsets.UTF_8));
    }

    protected void responderTexto(HttpExchange exchange, int status, String texto) {
        try {
            responder(exchange, status, "text/plain; charset=UTF-8",
                    texto.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignorado) {
            // resposta já foi enviada ou o cliente desconectou
        }
    }

    protected void responder(HttpExchange exchange, int status,
                             String tipo, byte[] dados) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", tipo);
        exchange.sendResponseHeaders(status, dados.length);
        exchange.getResponseBody().write(dados);
    }
}
