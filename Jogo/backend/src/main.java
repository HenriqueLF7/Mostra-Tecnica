import jogo.banco.ConexaoBanco;
import jogo.banco.JogadorRepository;
import jogo.banco.PartidaRepository;
import jogo.logica.GameLoop;
import jogo.logica.Jogo;
import jogo.servidor.GameServer;

import java.nio.file.Path;

/** Ponto de entrada: só cria os objetos e liga uns aos outros. */
public class Main {

    private static final int PORTA = 8080;
    private static final String ARQUIVO_BANCO = "backend/database/jogo.db";
    private static final Path PASTA_FRONTEND = Path.of("frontend");

    public static void main(String[] args) throws Exception {

        ConexaoBanco banco = new ConexaoBanco(ARQUIVO_BANCO);
        banco.criarTabelas();

        JogadorRepository jogadores = new JogadorRepository(banco);
        PartidaRepository partidas = new PartidaRepository(banco);

        Jogo jogo = new Jogo(jogadores, partidas);

        new GameServer(PORTA, jogo, partidas, PASTA_FRONTEND).iniciar();
        new Thread(new GameLoop(jogo), "game-loop").start();

        System.out.println("Servidor rodando em http://localhost:" + PORTA);
    }
}
