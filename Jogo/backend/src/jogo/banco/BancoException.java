package jogo.banco;

/** Erro ao falar com o banco (Supabase). */
public class BancoException extends Exception {

    public BancoException(String mensagem) {
        super(mensagem);
    }

    public BancoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
