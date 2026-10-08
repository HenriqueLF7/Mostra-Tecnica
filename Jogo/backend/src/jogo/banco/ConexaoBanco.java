package jogo.banco;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Sabe abrir conexões com o SQLite e criar as tabelas do jogo. */
public class ConexaoBanco {

    private final String url;

    public ConexaoBanco(String caminhoArquivo) {
        this.url = "jdbc:sqlite:" + caminhoArquivo;
    }

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void criarTabelas() {
        String sqlJogadores = """
            CREATE TABLE IF NOT EXISTS jogadores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL UNIQUE
            );
        """;

        String sqlPartidas = """
            CREATE TABLE IF NOT EXISTS partidas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                jogador_id INTEGER NOT NULL,
                kills INTEGER NOT NULL,
                tempo_segundos INTEGER NOT NULL,
                FOREIGN KEY (jogador_id) REFERENCES jogadores(id)
            );
        """;

        try (Connection conexao = conectar();
             Statement stmt = conexao.createStatement()) {

            stmt.execute(sqlJogadores);
            stmt.execute(sqlPartidas);

            System.out.println("Banco de dados OK.");

        } catch (SQLException e) {
            System.out.println("ERRO NO SQLITE:");
            e.printStackTrace();
        }
    }
}
