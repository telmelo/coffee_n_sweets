package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ClienteDAO;
import model.Cliente;
import util.Resposta;
import view.Layout;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ClienteHandler implements HttpHandler {
    private final ClienteDAO clienteDAO = new ClienteDAO();
    @Override
    public void handle(HttpExchange troca) throws IOException{
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        if (metodo.equals("GET")){
            switch (caminho) {
                case "/clientes" -> mostrarLista(troca);
                default -> Resposta.naoEncontrado(troca);
            }
        }
    }

    private void mostrarLista(HttpExchange troca) throws IOException{
        try {
            List<Cliente> clientes = clienteDAO.listar();
            String html = ClienteView.tabela(clientes);
            Resposta.html(troca, Layout.pagina("Clientes", html));
        }catch (SQLException e){
            e.printStackTrace();
            Resposta.erro(troca, e.getMessage());
        }
    }
}
