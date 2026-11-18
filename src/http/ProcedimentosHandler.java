package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ProcedimentosDAO;
import util.Resposta;
import view.Layout;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class ProcedimentosHandler implements HttpHandler {
    private static final Set<String> STATUS_VALIDOS = Set.of("Pendente", "Em preparo", "Pronto");

    private final ProcedimentosDAO dao = new ProcedimentosDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        try {
            if (metodo.equals("GET") && caminho.equals("/procedimentos")) {
                mostrar(troca);
            } else if (metodo.equals("POST")) {
                switch (caminho) {
                    case "/procedimentos/status" -> atualizarStatus(troca);
                    case "/procedimentos/distribuir" -> distribuir(troca);
                    default -> Resposta.naoEncontrado(troca);
                }
            } else {
                Resposta.naoEncontrado(troca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Resposta.erro(troca, e.getMessage());
        }
    }

    private void mostrar(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> query = Formulario.lerQuery(troca);
        String html = ProcedimentosView.pagina(
                query.get("msg"), "erro".equals(query.get("tipo")),
                dao.pedidos(), dao.preparosEmAndamento());
        Resposta.html(troca, Layout.pagina("Procedimentos", html));
    }

    private void atualizarStatus(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);
        int pedido = Formulario.inteiro(form.get("pedido"));
        String novo = form.getOrDefault("novo", "");
        if (pedido == 0 || !STATUS_VALIDOS.contains(novo)) {
            voltar(troca, "Escolha um pedido e um status válidos.", true);
            return;
        }
        String mensagem = dao.atualizarStatus(pedido, novo);
        boolean recusado = mensagem != null
                && (mensagem.startsWith("Transição inválida") || mensagem.startsWith("Pedido não encontrado"));
        voltar(troca, "Pedido #" + pedido + ": " + mensagem, recusado);
    }

    private void distribuir(HttpExchange troca) throws IOException, SQLException {
        Formulario.lerCorpo(troca);
        int qtd = dao.distribuirItensPendentes();
        String mensagem = qtd == 0
                ? "Nenhum item pendente sem responsável."
                : qtd + (qtd == 1 ? " item distribuído" : " itens distribuídos")
                + " entre os baristas e os pedidos passaram para \"Em preparo\".";
        voltar(troca, mensagem, false);
    }

    private void voltar(HttpExchange troca, String mensagem, boolean erro) throws IOException {
        Formulario.redirecionar(troca, "/procedimentos?tipo=" + (erro ? "erro" : "ok")
                + "&msg=" + URLEncoder.encode(mensagem, StandardCharsets.UTF_8));
    }
}
