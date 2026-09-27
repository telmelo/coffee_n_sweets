package main;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpContext;
import http.ClienteHandler;


public class Servidor {
    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/clientes", new ClienteHandler());
        servidor.setExecutor(null);
        servidor.start();
        System.out.println("Servidor esta no ar. http://localhost:8080/clientes");
    }
}
