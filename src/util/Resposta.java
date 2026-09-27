package util;

import com.sun.net.httpserver.HttpExchange;
import http.Html;
import view.Layout;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Resposta {
    private static void enviar(HttpExchange troca, int codigo, String tipo, byte[] corpo) throws IOException {
        troca.getResponseHeaders().set("Content-Type", tipo);
        troca.sendResponseHeaders(codigo, corpo.length);
        try (OutputStream saida = troca.getResponseBody()){
            saida.write(corpo);
        }
    }

    private static void paginaSimples(HttpExchange troca, int codigo, String titulo, String mensagem) throws IOException {
        String corpo = Layout.pagina(titulo, "<p>" + Html.escapar(mensagem) + "</p>");
        enviar(troca, codigo, "text/html; charset=UTF-8", corpo.getBytes(StandardCharsets.UTF_8));
    }

    public static void erro(HttpExchange troca, String mensagem) throws IOException {
        paginaSimples(troca, 500, "Erro interno", mensagem);
    }

    public static void naoEncontrado(HttpExchange troca) throws IOException {
        paginaSimples(troca, 404, "Página não encontrada", "O endereço acessado não existe.");
    }

    public static void naoPermitido(HttpExchange troca) throws IOException {
        paginaSimples(troca, 405, "Método não permitido", "Esta ação exige outro tipo de requisição.");
    }

    public static void html(HttpExchange troca, String conteudo) throws IOException {
        enviar(troca, 200, "text/html; charset=UTF-8", conteudo.getBytes(StandardCharsets.UTF_8));
    }

    public static void redirecionar(HttpExchange troca, String destino) throws IOException {
        troca.getResponseHeaders().set("Location", destino);
        troca.sendResponseHeaders(303, -1);
        troca.close();
    }
}
