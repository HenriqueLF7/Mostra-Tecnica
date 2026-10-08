package jogo.banco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Acesso à tabela "jogadores". */
public class JogadorRepository {

    private final ConexaoBanco banco;

    public JogadorRepository(ConexaoBanco banco) {
        this.banco = banco;
    }

    /** Retorna o id do jogador com esse nome, cadastrando-o se ainda não existir. */
    public int obterOuCriar(String nome) throws SQLException {
        try (Connection conexao = banco.conectar()) {

            try (PreparedStatement stmt = conexao.prepareStatement(
                    "SELECT id FROM jogadores WHERE nome = ?")) {

                stmt.setString(1, nome);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }

            try (PreparedStatement stmt = conexao.prepareStatement(
                    "INSERT INTO jogadores (nome) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, nome);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        throw new SQLException("Não foi possível criar o jogador.");
    }
}
