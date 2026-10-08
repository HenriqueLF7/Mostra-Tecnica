package jogo.banco;

import jogo.modelo.RankingEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela "partidas" e ao ranking. */
public class PartidaRepository {

    private static final int TAMANHO_RANKING = 5;

    private final ConexaoBanco banco;

    public PartidaRepository(ConexaoBanco banco) {
        this.banco = banco;
    }

    public void salvar(int jogadorId, int kills, int tempoSegundos) throws SQLException {
        String sql = """
            INSERT INTO partidas (jogador_id, kills, tempo_segundos)
            VALUES (?, ?, ?)
        """;

        try (Connection conexao = banco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, jogadorId);
            stmt.setInt(2, kills);
            stmt.setInt(3, tempoSegundos);
            stmt.executeUpdate();
        }
    }

    /** As melhores partidas: mais kills primeiro; empate decide pelo maior tempo. */
    public List<RankingEntry> buscarRanking() throws SQLException {
        String sql = """
            SELECT jogadores.nome, partidas.kills, partidas.tempo_segundos
            FROM partidas
            INNER JOIN jogadores ON jogadores.id = partidas.jogador_id
            ORDER BY partidas.kills DESC, partidas.tempo_segundos DESC
            LIMIT ?
        """;

        List<RankingEntry> ranking = new ArrayList<>();

        try (Connection conexao = banco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, TAMANHO_RANKING);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ranking.add(new RankingEntry(
                            rs.getString("nome"),
                            rs.getInt("kills"),
                            rs.getInt("tempo_segundos")
                    ));
                }
            }
        }

        return ranking;
    }
}
