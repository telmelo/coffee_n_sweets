package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class MusicaHandler implements HttpHandler {
    private static final String PREFIXO = "/audio/";
    private static final Pattern NOME_VALIDO = Pattern.compile("musica[1-6]\\.mp3");

    @Override
    public void handle(HttpExchange troca) throws IOException {
        if (!troca.getRequestMethod().equals("GET")) {
            troca.sendResponseHeaders(405, -1);
            return;
        }

        String nome = troca.getRequestURI().getPath().substring(PREFIXO.length());
        if (!NOME_VALIDO.matcher(nome).matches()) {
            troca.sendResponseHeaders(404, -1);
            return;
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of("web", nome));
        } catch (IOException e) {
            troca.sendResponseHeaders(404, -1);
            return;
        }

        troca.getResponseHeaders().set("Content-Type", "audio/mpeg");
        troca.getResponseHeaders().set("Accept-Ranges", "bytes");
        troca.sendResponseHeaders(200, bytes.length);
        try (var saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }
}