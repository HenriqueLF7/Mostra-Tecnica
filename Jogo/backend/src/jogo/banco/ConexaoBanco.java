package jogo.banco;

import jogo.util.JsonLeitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

/**
 * Conexão com o Supabase (API REST / PostgREST).
 *
 * A URL e a chave ficam em "supabase.properties" (ao lado do jogo) ou nas variáveis
 * de ambiente SUPABASE_URL e SUPABASE_KEY. Assim o ranking é o mesmo em todas as máquinas.
 */
public class ConexaoBanco {

    private final String baseUrl;
    private final String chave;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public ConexaoBanco(Path arquivoConfig) {
        Properties props = new Properties();
        if (Files.exists(arquivoConfig)) {
            try (InputStream in = Files.newInputStream(arquivoConfig)) {
                props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Não consegui ler " + arquivoConfig + ": " + e.getMessage());
            }
        }

        String url = valor("SUPABASE_URL", props, "supabase.url");
        String key = valor("SUPABASE_KEY", props, "supabase.key");

        this.baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.chave = key;
    }

    private static String valor(String env, Properties props, String nome) {
        String v = System.getenv(env);
        if (v == null || v.isBlank()) {
            v = props.getProperty(nome, "");
        }
        return v.trim();
    }

    public boolean configurado() {
        return !baseUrl.isEmpty() && !chave.isEmpty()
                && !baseUrl.contains("SEU-PROJETO") && !chave.startsWith("COLE_");
    }

    /** Testa a conexão e avisa no console se algo estiver errado. */
    public void verificar() {
        if (!configurado()) {
            System.out.println("ATENCAO: Supabase nao configurado. Edite o arquivo supabase.properties.");
            return;
        }
        try {
            get("/rest/v1/jogadores?select=id&limit=1");
            System.out.println("Supabase OK (" + baseUrl + ").");
        } catch (BancoException e) {
            System.out.println("ERRO NO SUPABASE: " + e.getMessage());
        }
    }

    public static String codificar(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** GET num caminho REST; devolve o JSON já interpretado. */
    public Object get(String caminho) throws BancoException {
        return enviar(montar(caminho).GET().build());
    }

    /** POST com corpo JSON. "prefer" é o cabeçalho Prefer do PostgREST (pode ser null). */
    public Object post(String caminho, String json, String prefer) throws BancoException {
        HttpRequest.Builder b = montar(caminho)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        if (prefer != null) {
            b.header("Prefer", prefer);
        }
        return enviar(b.build());
    }

    private HttpRequest.Builder montar(String caminho) throws BancoException {
        if (!configurado()) {
            throw new BancoException("Supabase não configurado (veja supabase.properties).");
        }
        return HttpRequest.newBuilder(URI.create(baseUrl + caminho))
                .timeout(Duration.ofSeconds(8))
                .header("apikey", chave)
                .header("Authorization", "Bearer " + chave)
                .header("Accept", "application/json");
    }

    private Object enviar(HttpRequest req) throws BancoException {
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                throw new BancoException("Supabase respondeu " + resp.statusCode() + ": " + resp.body());
            }
            String corpo = resp.body();
            return (corpo == null || corpo.isBlank()) ? null : JsonLeitor.ler(corpo);
        } catch (IOException e) {
            throw new BancoException("Sem conexão com o Supabase: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BancoException("Requisição interrompida", e);
        }
    }
}
