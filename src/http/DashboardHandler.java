package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ClienteDAO;
import dao.ConsultasDAO;
import dao.ProdutoDAO;
import dto.BaristasDestaqueDTO;
import dto.CafesEGraosDTO;
import dto.FaturamentoDTO;
import dto.TopProdutosDTO;
import util.Resposta;
import view.Layout;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardHandler implements HttpHandler {
    private final ConsultasDAO consultasDAO = new ConsultasDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        if (!metodo.equals("GET") || !caminho.equals("/dashboard")) {
            Resposta.naoEncontrado(troca);
            return;
        }

        try {
            List<FaturamentoDTO> faturamento = consultasDAO.faturamentoPorDia();
            List<TopProdutosDTO> topProdutos = consultasDAO.top10ProdutosVendidos();
            List<BaristasDestaqueDTO> baristas = consultasDAO.baristasAcimaDaMedia();
            List<CafesEGraosDTO> cafes = consultasDAO.cafesEGraos();

            boolean graficosPython = GraficosPython.gerar(faturamento, topProdutos, baristas);

            String html = DashboardView.pagina(
                    faturamento, topProdutos, baristas, cafes,
                    clienteDAO.listar().size(),
                    produtoDAO.listar().size(),
                    graficosPython
            );
            Resposta.html(troca, Layout.pagina("Dashboard", html));
        } catch (SQLException e) {
            e.printStackTrace();
            Resposta.erro(troca, e.getMessage());
        }
    }
}