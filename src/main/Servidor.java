package main;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpContext;
import http.*;

public class Servidor {
    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/clientes", new ClienteHandler());
        servidor.createContext("/produtos", new ProdutoHandler());
        servidor.createContext("/audio/", new MusicaHandler());
        servidor.createContext("/mesas", new MesaHandler());
        servidor.createContext("/funcionarios", new FuncionarioHandler());
        servidor.createContext("/api/dashboard", new DashboardApiHandler());
        servidor.createContext("/api/boxplot", new DashboardApiHandler());
        servidor.createContext("/dashboard", (exchange) -> {
            String resposta = DashboardView.pagina();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] bytes = resposta.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (var os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        servidor.createContext("/api/views", new ViewsApiHandler());
        servidor.createContext("/views", (exchange) -> {
            byte[] bytes = ViewsView.pagina().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var os = exchange.getResponseBody()) { os.write(bytes); }
        });
        servidor.createContext("/graficos", new ArquivoEstaticoHandler("/graficos", Path.of("graficos_gerados")));
        servidor.createContext("/procedimentos", new ProcedimentosHandler());
        servidor.createContext("/", new ArquivoEstaticoHandler("/", Path.of("web")));
        servidor.setExecutor(null);
        servidor.start();
        System.out.println("Servidor esta no ar. http://localhost:8080/clientes");
    }
}
