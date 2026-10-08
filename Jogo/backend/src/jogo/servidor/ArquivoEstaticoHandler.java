package jogo.servidor;

import com.sun.net.httpserver.HttpExchange;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Entrega os arquivos do frontend (html, css, js, imagens). */
public class ArquivoEstaticoHandler extends BaseHandler {

    private static final Map<String, String> TIPOS = Map.of(
            ".html", "text/html; charset=UTF-8",
            ".css", "text/css; charset=UTF-8",
            ".js", "application/javascript; charset=UTF-8",
            ".png", "image/png",
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".svg", "image/svg+xml",
            ".ico", "image/x-icon"
    );

    private final Path raiz;

    public ArquivoEstaticoHandler(Path pastaFrontend) {
        this.raiz = pastaFrontend.toAbsolutePath().normalize();
    }

    @Override
    protected void tratar(HttpExchange exchange) throws Exception {
        String caminho = exchange.getRequestURI().getPath();

        if (caminho.equals("/")) {
            caminho = "/index.html";
        }

        Path arquivo = raiz.resolve(caminho.substring(1)).normalize();

        // segurança: não deixa acessar arquivos fora da pasta do frontend (ex.: /../)
        if (!arquivo.startsWith(raiz) || !Files.isRegularFile(arquivo)) {
            responderTexto(exchange, 404, "Arquivo não encontrado.");
            return;
        }

        responder(exchange, 200, descobrirTipo(arquivo), Files.readAllBytes(arquivo));
    }

    private String descobrirTipo(Path arquivo) {
        String nome = arquivo.getFileName().toString().toLowerCase();

        for (Map.Entry<String, String> tipo : TIPOS.entrySet()) {
            if (nome.endsWith(tipo.getKey())) {
                return tipo.getValue();
            }
        }
        return "text/plain; charset=UTF-8";
    }
}
