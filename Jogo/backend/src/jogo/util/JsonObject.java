package jogo.util;

/**
 * Construtor de objetos JSON.
 *
 * Exemplo: JsonObject.novo().campo("x", 10).campo("nome", "Ana").construir()
 */
public final class JsonObject {

    private final StringBuilder corpo = new StringBuilder("{");
    private boolean vazio = true;

    private JsonObject() {
    }

    public static JsonObject novo() {
        return new JsonObject();
    }

    private StringBuilder abrirCampo(String nome) {
        if (!vazio) {
            corpo.append(", ");
        }
        vazio = false;

        return corpo.append('"').append(Json.escapar(nome)).append("\": ");
    }

    public JsonObject campo(String nome, int valor) {
        abrirCampo(nome).append(valor);
        return this;
    }

    public JsonObject campo(String nome, long valor) {
        abrirCampo(nome).append(valor);
        return this;
    }

    /** Número decimal com 1 casa (suficiente para posições em pixels). */
    public JsonObject campo(String nome, double valor) {
        abrirCampo(nome).append(String.format(java.util.Locale.ROOT, "%.1f", valor));
        return this;
    }

    public JsonObject campo(String nome, boolean valor) {
        abrirCampo(nome).append(valor);
        return this;
    }

    public JsonObject campo(String nome, String valor) {
        abrirCampo(nome).append('"').append(Json.escapar(valor)).append('"');
        return this;
    }

    /** Insere um trecho que já é JSON válido (um array, outro objeto...). */
    public JsonObject campoJson(String nome, String jsonPronto) {
        abrirCampo(nome).append(jsonPronto);
        return this;
    }

    public String construir() {
        return corpo + " }";
    }
}
