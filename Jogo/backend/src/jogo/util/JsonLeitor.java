package jogo.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Leitor de JSON mínimo (sem bibliotecas externas).
 * Devolve Map, List, String, Double, Boolean ou null.
 */
public final class JsonLeitor {

    private final String texto;
    private int pos;

    private JsonLeitor(String texto) {
        this.texto = texto;
    }

    public static Object ler(String json) {
        JsonLeitor leitor = new JsonLeitor(json);
        leitor.pular();
        Object valor = leitor.valor();
        leitor.pular();
        if (leitor.pos != json.length()) {
            throw new IllegalArgumentException("JSON inválido na posição " + leitor.pos);
        }
        return valor;
    }

    private void pular() {
        while (pos < texto.length() && Character.isWhitespace(texto.charAt(pos))) {
            pos++;
        }
    }

    private Object valor() {
        if (pos >= texto.length()) {
            throw new IllegalArgumentException("JSON terminou antes do esperado");
        }
        char c = texto.charAt(pos);
        if (c == '{') return objeto();
        if (c == '[') return lista();
        if (c == '"') return string();
        if (texto.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
        if (texto.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        if (texto.startsWith("null", pos)) { pos += 4; return null; }
        return numero();
    }

    private Map<String, Object> objeto() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        pos++; // {
        pular();
        if (texto.charAt(pos) == '}') { pos++; return mapa; }
        while (true) {
            pular();
            String chave = string();
            pular();
            esperar(':');
            pular();
            mapa.put(chave, valor());
            pular();
            if (texto.charAt(pos) == ',') { pos++; continue; }
            esperar('}');
            return mapa;
        }
    }

    private List<Object> lista() {
        List<Object> itens = new ArrayList<>();
        pos++; // [
        pular();
        if (texto.charAt(pos) == ']') { pos++; return itens; }
        while (true) {
            pular();
            itens.add(valor());
            pular();
            if (texto.charAt(pos) == ',') { pos++; continue; }
            esperar(']');
            return itens;
        }
    }

    private String string() {
        esperar('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = texto.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            char e = texto.charAt(pos++);
            switch (e) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
                    pos += 4;
                }
                default -> sb.append(e); // \" \\ \/
            }
        }
    }

    private Double numero() {
        int ini = pos;
        while (pos < texto.length() && "+-0123456789.eE".indexOf(texto.charAt(pos)) >= 0) {
            pos++;
        }
        if (ini == pos) {
            throw new IllegalArgumentException("JSON inválido na posição " + pos);
        }
        return Double.parseDouble(texto.substring(ini, pos));
    }

    private void esperar(char c) {
        if (pos >= texto.length() || texto.charAt(pos) != c) {
            throw new IllegalArgumentException("Esperava '" + c + "' na posição " + pos);
        }
        pos++;
    }
}
