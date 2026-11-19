package http;

import java.util.LinkedHashMap;
import java.util.Map;

/** Gerador mínimo de JSON (sem bibliotecas externas): aceita Map, Iterable, Number, Boolean, null e texto/datas. */
public final class Json {
    private Json() {}

    /** Monta um objeto JSON a partir de pares chave, valor, chave, valor... */
    public static Map<String, Object> obj(Object... pares) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) {
            mapa.put(String.valueOf(pares[i]), pares[i + 1]);
        }
        return mapa;
    }

    public static String escrever(Object valor) {
        StringBuilder sb = new StringBuilder();
        escrever(sb, valor);
        return sb.toString();
    }

    private static void escrever(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Double d) {
            sb.append(d.isNaN() || d.isInfinite() ? "null" : d.toString());
        } else if (v instanceof Number) {
            sb.append(v);
        } else if (v instanceof Map<?, ?> mapa) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> e : mapa.entrySet()) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                texto(sb, String.valueOf(e.getKey()));
                sb.append(':');
                escrever(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Iterable<?> itens) {
            sb.append('[');
            boolean primeiro = true;
            for (Object item : itens) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escrever(sb, item);
            }
            sb.append(']');
        } else {
            texto(sb, v.toString()); // String, LocalDate, LocalDateTime...
        }
    }

    private static void texto(StringBuilder sb, String valor) {
        sb.append('"');
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }
}