package http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitários para trabalhar com formulários HTML:
 * - lerCorpo: lê os campos enviados por POST (application/x-www-form-urlencoded)
 * - lerQuery: lê os parâmetros da URL (ex: /clientes/editar?id=5)
 * - redirecionar: responde com um redirecionamento (padrão Post/Redirect/Get)
 */
public class Formulario {

    public static Map<String, String> lerCorpo(HttpExchange troca) throws IOException {
        String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return separarParametros(corpo);
    }

    public static Map<String, String> lerQuery(HttpExchange troca) {
        return separarParametros(troca.getRequestURI().getRawQuery());
    }

    private static Map<String, String> separarParametros(String texto) {
        Map<String, String> parametros = new HashMap<>();
        if (texto == null || texto.isEmpty()) {
            return parametros;
        }
        for (String par : texto.split("&")) {
            String[] partes = par.split("=", 2);
            String chave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length > 1 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            parametros.put(chave, valor);
        }
        return parametros;
    }

    /** Converte texto em int; devolve 0 se for nulo ou inválido. */
    public static int inteiro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /** Redireciona o navegador para outra página (HTTP 303). */
    public static void redirecionar(HttpExchange troca, String destino) throws IOException {
        troca.getResponseHeaders().set("Location", destino);
        troca.sendResponseHeaders(303, -1);
        troca.close();
    }
}