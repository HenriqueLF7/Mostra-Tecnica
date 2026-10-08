package jogo.modelo;

/** Para que lado o personagem está olhando / atirando. */
public enum Lado {

    ESQUERDA,
    DIREITA;

    /** Converte o texto vindo do front. Qualquer coisa diferente de "esquerda" vira DIREITA. */
    public static Lado deTexto(String texto) {
        return "esquerda".equalsIgnoreCase(texto) ? ESQUERDA : DIREITA;
    }

    public String texto() {
        return name().toLowerCase();
    }
}
