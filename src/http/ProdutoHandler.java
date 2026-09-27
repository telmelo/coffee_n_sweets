package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ProdutoDAO;
import model.Produto;
import util.Resposta;
import view.Layout;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProdutoHandler implements HttpHandler {
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        if (metodo.equals("GET")) {
            switch (caminho) {
                case "/produtos" -> mostrarLista(troca);
                default -> Resposta.naoEncontrado(troca);
            }
        }
    }

    private void mostrarLista(HttpExchange troca) throws IOException {
        try {
            List<Produto> produtos = produtoDAO.listar();
            String html = ProdutoView.tabela(produtos);
            Resposta.html(troca, Layout.pagina("Produtos", html));
        } catch (SQLException e) {
            e.printStackTrace();
            Resposta.erro(troca, e.getMessage());
        }
    }
}