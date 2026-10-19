package jogo.banco;

import jogo.util.Json;

import java.util.List;
import java.util.Map;

/** Acesso à tabela "jogadores" no Supabase. */
public class JogadorRepository {

    private final ConexaoBanco banco;

    public JogadorRepository(ConexaoBanco banco) {
        this.banco = banco;
    }

    /** Retorna o id do jogador com esse nome, cadastrando-o se ainda não existir. */
    public int obterOuCriar(String nome) throws BancoException {
        String corpo = "{\"nome\": \"" + Json.escapar(nome) + "\"}";

        // Upsert: se o nome já existe, devolve a linha existente; senão cria.
        Object resp = banco.post("/rest/v1/jogadores?on_conflict=nome", corpo,
                "resolution=merge-duplicates,return=representation");

        if (resp instanceof List<?> lista && !lista.isEmpty()
                && lista.get(0) instanceof Map<?, ?> linha
                && linha.get("id") instanceof Number id) {
            return id.intValue();
        }
        throw new BancoException("Não foi possível criar o jogador.");
    }
}
