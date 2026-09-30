import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private static Player player = new Player(100, 100);
    private static java.util.List<Tiro> tiros = new java.util.concurrent.CopyOnWriteArrayList<>();

    private static volatile int ultimaLarguraJanela = 1280;
    private static volatile int ultimaAlturaJanela = 720;

    // controle simples do cooldown de 2s entre tiros
    private static final long COOLDOWN_TIRO_MS = 2000;
    private static long ultimoDisparo = -COOLDOWN_TIRO_MS;

    private static int kills = 0;
    private static long ultimoDanoNoPlayer = 0;

        private static final java.util.List<Zombie> zumbis =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    private static volatile int onda = 0;
    private static long momentoOndaLimpa = 0;

    private static final long PAUSA_ENTRE_ONDAS_MS = 1000;
    private static final int MAX_ZUMBIS_POR_ONDA = 24;

    private static final long INVULNERABILIDADE_MS = 1000;

    // Hitbox do player (mesmos valores do clamp no Player.java)
    private static final int PLAYER_LARGURA = 90;
    private static final int PLAYER_ALTURA = 100;

    private static volatile boolean jogoAtivo = false;
    
    public static void main(String[] args) throws Exception {

    Database.criarTabelas();

    HttpServer server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        server.createContext("/", Main::pagina);

        server.createContext("/player", Main::player);

        server.createContext("/tiro", Main::atirar);

        server.createContext("/reiniciar", Main::reiniciar);

        server.createContext("/iniciar", ex -> definirAtivo(ex, true));
        
        server.createContext("/parar", ex -> definirAtivo(ex, false));

        server.start();
        
        Thread jogo = new Thread(() -> {

            while (true) {

                long agora = System.currentTimeMillis();

                if (jogoAtivo && player.isVivo()) {

                    // zumbis andam até o player e se afastam entre si
                    for (Zombie z : zumbis) {
                        z.moverEmDirecao(player.getX(), player.getY());
                    }
                    separarZumbis();

                    // tiros: mover, sair da tela, acertar algum zumbi
                    for (Tiro t : tiros) {
                        t.mover();

                        if (t.saiuDaTela(ultimaLarguraJanela, ultimaAlturaJanela)) {
                            tiros.remove(t);
                            continue;
                        }

                        for (Zombie z : zumbis) {
                            if (z.isVivo() && z.colideComPonto(t.getX(), t.getY())) {
                                z.receberDano(t.getDano());
                                tiros.remove(t);

                                if (!z.isVivo()) {
                                    kills++;
                                }
                                break;
                            }
                        }
                    }

                    zumbis.removeIf(z -> !z.isVivo());

                    // onda limpa: espera um pouco e manda a próxima (dobro)
                    if (zumbis.isEmpty()) {
                        if (momentoOndaLimpa == 0) {
                            momentoOndaLimpa = agora;
                        } else if (agora - momentoOndaLimpa >= PAUSA_ENTRE_ONDAS_MS) {
                            spawnarOnda();
                            momentoOndaLimpa = 0;
                        }
                    }

                    // algum zumbi encostou no player
                    for (Zombie z : zumbis) {
                        if (z.colideComRetangulo(player.getX(), player.getY(),
                                                 PLAYER_LARGURA, PLAYER_ALTURA)
                                && agora - ultimoDanoNoPlayer >= INVULNERABILIDADE_MS) {

                            player.vidaPlayer(-1);
                            ultimoDanoNoPlayer = agora;

                            if (!player.isVivo()) jogoAtivo = false;
                            break;
                        }
                    }
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

jogo.start();

        System.out.println("ta funcionando porra");
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

    private static void player(HttpExchange exchange) throws IOException {

if (exchange.getRequestMethod().equals("POST")) {

    String corpo = new String(
            exchange.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
    ).trim();

    String[] partes = corpo.split(";");

    String comando = partes[0].toUpperCase();
    int larguraJanela = Integer.parseInt(partes[1]);
    int alturaJanela = Integer.parseInt(partes[2]);

        if (jogoAtivo) {
            player.movimentacaoPlayer(comando, larguraJanela, alturaJanela);
    }

    ultimaLarguraJanela = larguraJanela;
    ultimaAlturaJanela = alturaJanela;
}

    String resposta =
        "{ \"x\": " + player.getX() +
        ", \"y\": " + player.getY() +
        ", \"zumbis\": " + zumbisParaJson() +
        ", \"onda\": " + onda +
        ", \"pontosVida\": " + player.getPontosVida() +
        ", \"kills\": " + kills +
        ", \"tiros\": " + tirosParaJson() +
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
    private static void atirar(HttpExchange exchange) throws IOException {

    boolean disparou = false;

    if (exchange.getRequestMethod().equals("POST")) {

        String corpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        ).trim();

                // formato: "direcao;largura;altura;mouseX;mouseY"
        String[] partes = corpo.split(";");

        String direcao = partes.length > 0 && !partes[0].isEmpty()
                ? partes[0]
                : "direita";

        if (partes.length > 2) {
            ultimaLarguraJanela = Integer.parseInt(partes[1]);
            ultimaAlturaJanela = Integer.parseInt(partes[2]);
        }

        // alvo padrão: reto pro lado que ele olha (caso não venha o mouse)
        int alvoX = "esquerda".equals(direcao) ? 0 : ultimaLarguraJanela;
        int alvoY = player.getY() + Tiro.MUZZLE_OFFSET_Y;

        if (partes.length > 4) {
            alvoX = Integer.parseInt(partes[3]);
            alvoY = Integer.parseInt(partes[4]);
        }

        long agora = System.currentTimeMillis();

        if (jogoAtivo && agora - ultimoDisparo >= COOLDOWN_TIRO_MS) { {
            ultimoDisparo = agora;
            disparou = true;

            tiros.add(Tiro.criarNaPontaDaArma(
                    player.getX(),
                    player.getY(),
                    alvoX,
                    alvoY,
                    direcao
            ));
        }
    }

    long cooldownRestante = Math.max(
            0,
            COOLDOWN_TIRO_MS - (System.currentTimeMillis() - ultimoDisparo)
    );

    String resposta =
        "{ \"disparou\": " + disparou +
        ", \"cooldownRestante\": " + cooldownRestante +
        ", \"tiros\": " + tirosParaJson() +
        " }";

    byte[] dados = resposta.getBytes(StandardCharsets.UTF_8);

    exchange.getResponseHeaders().set("Content-Type", "application/json");

    exchange.sendResponseHeaders(200, dados.length);

    exchange.getResponseBody().write(dados);

    exchange.close();
}
    }
     private static void reiniciar(HttpExchange exchange) throws IOException {

        jogoAtivo = false;

        int largura = ultimaLarguraJanela;
        int altura = ultimaAlturaJanela;

        if (exchange.getRequestMethod().equals("POST")) {
            String corpo = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();

            String[] partes = corpo.split(";");
            if (partes.length > 1) {
                largura = Integer.parseInt(partes[0]);
                altura = Integer.parseInt(partes[1]);
            }
        }

        ultimaLarguraJanela = largura;
        ultimaAlturaJanela = altura;

        // player nasce no centro da tela
        Player novoPlayer = new Player(
                (largura - PLAYER_LARGURA) / 2,
                (altura - PLAYER_ALTURA) / 2
        );



        tiros.clear();
        kills = 0;
        ultimoDanoNoPlayer = System.currentTimeMillis();
        ultimoDisparo = -COOLDOWN_TIRO_MS;

        player = novoPlayer;
        zumbis.clear();
        onda = 0;
        momentoOndaLimpa = 0;

        byte[] dados = "{}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, dados.length);
        exchange.getResponseBody().write(dados);
        exchange.close();
    }

        private static void definirAtivo(HttpExchange exchange, boolean ativo) throws IOException {

        if (ativo) {
            // invulnerabilidade de 1s a partir do momento em que o jogo começa
            ultimoDanoNoPlayer = System.currentTimeMillis();
        }
        jogoAtivo = ativo;

        byte[] dados = "{}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, dados.length);
        exchange.getResponseBody().write(dados);
        exchange.close();
    }


    private static void spawnarOnda() {
        onda++;

        int quantidade = (int) Math.min(MAX_ZUMBIS_POR_ONDA, Math.pow(2, onda - 1));
        int velocidadeBase = 6 + Math.min((onda - 1) / 2, 2);   // 7, 7, 8, 8, 9...

        for (int i = 0; i < quantidade; i++) {
            Zombie z = new Zombie(0, 0);

            // cada zumbi com uma velocidade um pouco diferente (-1, 0 ou +1)
            z.setVelocidade(velocidadeBase + (int) (Math.random() * 3) - 1);

            // tenta achar um ponto de nascimento longe dos outros zumbis
            for (int tentativa = 0; tentativa < 20; tentativa++) {
                z.renascer(ultimaLarguraJanela, ultimaAlturaJanela,
                           player.getX(), player.getY());

                boolean livre = true;
                for (Zombie outro : zumbis) {
                    int dx = outro.getX() - z.getX();
                    int dy = outro.getY() - z.getY();
                    if (Math.sqrt(dx * dx + dy * dy) < 110) {
                        livre = false;
                        break;
                    }
                }
                if (livre) break;
            }

            zumbis.add(z);
        }
    }

    private static final int DISTANCIA_ENTRE_ZUMBIS = 80;
    private static final int EMPURRAO_MAX = 8;

    // Empurra os zumbis que estão perto demais uns dos outros
    private static void separarZumbis() {
        Zombie[] arr = zumbis.toArray(new Zombie[0]);

        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                Zombie a = arr[i];
                Zombie b = arr[j];

                double dx = (a.getX() + Zombie.LARGURA / 2.0) - (b.getX() + Zombie.LARGURA / 2.0);
                double dy = (a.getY() + Zombie.ALTURA / 2.0) - (b.getY() + Zombie.ALTURA / 2.0);
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist >= DISTANCIA_ENTRE_ZUMBIS) continue;

                if (dist < 1) {
                    // exatamente no mesmo ponto: sorteia uma direção
                    double ang = i * 31 + j * 17;
                    dx = Math.cos(ang);
                    dy = Math.sin(ang);
                    dist = 1;
                }

                double empurrao = Math.min(EMPURRAO_MAX, (DISTANCIA_ENTRE_ZUMBIS - dist) / 2);
                int mx = (int) Math.round(dx / dist * empurrao);
                int my = (int) Math.round(dy / dist * empurrao);

                a.deslocar(mx, my);
                b.deslocar(-mx, -my);
            }
        }

        for (Zombie z : arr) {
            z.limitar(ultimaLarguraJanela, ultimaAlturaJanela);
        }
            }

    private static String zumbisParaJson() {
        StringBuilder json = new StringBuilder("[");
        boolean primeiro = true;

        for (Zombie z : zumbis) {
            if (!primeiro) json.append(",");
            primeiro = false;
            json.append("{ \"x\": ").append(z.getX())
                .append(", \"y\": ").append(z.getY())
                .append(" }");
        }

        return json.append("]").toString();
    }

    private static String tirosParaJson() {

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < tiros.size(); i++) {
            if (i > 0) json.append(",");
            json.append(tiros.get(i).toJson());
        }

        json.append("]");

        return json.toString();
}
}