package jogo.util;

import java.util.Collection;
import java.util.StringJoiner;

/** Funções utilitárias de JSON (não precisa de biblioteca externa). */
public final class Json {

    private Json() {
    }

    /** Escapa aspas, barras e caracteres de controle de um texto. */
    public static String escapar(String texto) {
        StringBuilder sb = new StringBuilder();

        for (char c : texto.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }

        return sb.toString();
    }

    /** Monta um array JSON a partir de uma coleção de objetos serializáveis. */
    public static String array(Collection<? extends JsonSerializavel> itens) {
        StringJoiner juntador = new StringJoiner(",", "[", "]");

        for (JsonSerializavel item : itens) {
            juntador.add(item.toJson());
        }

        return juntador.toString();
    }
}
