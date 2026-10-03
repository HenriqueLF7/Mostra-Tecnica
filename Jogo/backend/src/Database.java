import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String URL =
            "jdbc:sqlite:backend/database/jogo.db";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void criarTabelas() {

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
                FOREIGN KEY (jogador_id)
                REFERENCES jogadores(id)
            );
        """;

        try (Connection conexao = conectar();
             Statement stmt = conexao.createStatement()) {

            System.out.println("CONEXAO COM SQLITE OK!");

            stmt.execute(sqlJogadores);
            stmt.execute(sqlPartidas);

            System.out.println("TABELAS CRIADAS!");

        } catch (SQLException e) {

            System.out.println("ERRO NO SQLITE:");
            e.printStackTrace();
        }
    }

    // =========================================================
    // CADASTRA O JOGADOR OU RETORNA O ID DELE
    // =========================================================

    public static int obterOuCriarJogador(String nome) throws SQLException {

        String sqlBuscar = """
            SELECT id
            FROM jogadores
            WHERE nome = ?
        """;

        try (Connection conexao = conectar();
             PreparedStatement stmt = conexao.prepareStatement(sqlBuscar)) {

            stmt.setString(1, nome);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        String sqlCriar = """
            INSERT INTO jogadores (nome)
            VALUES (?)
        """;

        try (Connection conexao = conectar();
             PreparedStatement stmt = conexao.prepareStatement(
                     sqlCriar,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nome);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Não foi possível criar o jogador.");
    }

    // =========================================================
    // SALVA UMA PARTIDA
    // =========================================================

    public static void salvarPartida(
            int jogadorId,
            int kills,
            int tempoSegundos) throws SQLException {

        String sql = """
            INSERT INTO partidas (
                jogador_id,
                kills,
                tempo_segundos
            )
            VALUES (?, ?, ?)
        """;

        try (Connection conexao = conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, jogadorId);
            stmt.setInt(2, kills);
            stmt.setInt(3, tempoSegundos);

            stmt.executeUpdate();
        }
    }

    // =========================================================
    // BUSCA O TOP 5
    // =========================================================

    public static List<Ranking> buscarRanking() throws SQLException {

        List<Ranking> ranking = new ArrayList<>();

        String sql = """
            SELECT
                jogadores.nome,
                partidas.kills,
                partidas.tempo_segundos
            FROM partidas
            INNER JOIN jogadores
                ON jogadores.id = partidas.jogador_id
            ORDER BY
                partidas.kills DESC,
                partidas.tempo_segundos DESC
            LIMIT 5
        """;

        try (Connection conexao = conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String nome = rs.getString("nome");
                int kills = rs.getInt("kills");
                int tempo = rs.getInt("tempo_segundos");

                ranking.add(
                    new Ranking(nome, kills, tempo)
                );
            }
        }

        return ranking;
    }

    // =========================================================
    // CLASSE DO RANKING
    // =========================================================

    public static class Ranking {

        public String nome;
        public int kills;
        public int tempoSegundos;

        public Ranking(
                String nome,
                int kills,
                int tempoSegundos) {

            this.nome = nome;
            this.kills = kills;
            this.tempoSegundos = tempoSegundos;
        }
    }
}