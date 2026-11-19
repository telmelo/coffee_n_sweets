package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.DashboardDAO;
import dao.ViewsDAO;
import util.Resposta;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

/** GET /api/views?inicio=&fim=&forma=&tipo=&barista=&equipamento=  → dados das duas views. */
public class ViewsApiHandler implements HttpHandler {
    private final ViewsDAO viewsDAO = new ViewsDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        if (!troca.getRequestMethod().equals("GET") || !troca.getRequestURI().getPath().equals("/api/views")) {
            Resposta.naoEncontrado(troca);
            return;
        }
        try {
            responder(troca, 200, montar(parametros(troca)));
        } catch (IllegalArgumentException e) {
            responder(troca, 400, Json.obj("erro", e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            responder(troca, 500, Json.obj("erro", "Erro no banco de dados: " + e.getMessage()));
        }
    }

    private Object montar(Map<String, String> q) throws SQLException {
        LocalDate[] intervalo = dashboardDAO.intervaloDatas();
        LocalDate inicio = data(q.get("inicio"), intervalo[0]);
        LocalDate fim = data(q.get("fim"), intervalo[1]);
        if (inicio.isAfter(fim)) throw new IllegalArgumentException("A data inicial não pode ser depois da data final.");

        String forma = opcional(q.get("forma"));
        if (forma != null && !DashboardDAO.FORMAS_PAGAMENTO.contains(forma))
            throw new IllegalArgumentException("Forma de pagamento inválida.");
        String tipo = opcional(q.get("tipo"));
        if (tipo != null && !ViewsDAO.TIPOS_CONSUMO.contains(tipo))
            throw new IllegalArgumentException("Tipo de consumo inválido.");
        Integer barista = inteiro(q.get("barista"));
        Integer equipamento = inteiro(q.get("equipamento"));

        var vendas = viewsDAO.vendas(inicio, fim, forma, tipo);
        var producao = viewsDAO.producao(barista, equipamento);

        return Json.obj(
                "filtro", Json.obj("inicio", inicio, "fim", fim, "forma", forma, "tipo", tipo,
                        "minimo", intervalo[0], "maximo", intervalo[1],
                        "formas", DashboardDAO.FORMAS_PAGAMENTO, "tipos", ViewsDAO.TIPOS_CONSUMO,
                        "barista", barista, "equipamento", equipamento),
                "baristas", lista(viewsDAO.baristas(), o -> Json.obj("id", o.id(), "nome", o.nome())),
                "equipamentos", lista(viewsDAO.equipamentos(), o -> Json.obj("id", o.id(), "nome", o.nome())),
                "vendas", lista(vendas, v -> Json.obj(
                        "pedido", v.pedido(), "item", v.item(), "data", v.data(), "horario", v.horario(),
                        "tipo", v.tipo(), "forma", v.forma(), "cliente", v.cliente(), "atendente", v.atendente(),
                        "mesa", v.mesa(), "produto", v.produto(), "quantidade", v.quantidade(),
                        "preco", v.preco(), "subtotal", v.subtotal())),
                "producao", lista(producao, p -> Json.obj(
                        "barista", p.barista(), "equipamento", p.equipamento(), "itens", p.itens(),
                        "unidades", p.unidades(), "produtos", p.produtos(),
                        "primeiro", p.primeiro(), "ultimo", p.ultimo())));
    }

    private static String opcional(String s) { return s == null || s.isBlank() ? null : s; }

    private static Integer inteiro(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.valueOf(s.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Filtro inválido: " + s); }
    }

    private static LocalDate data(String texto, LocalDate padrao) {
        if (texto == null || texto.isBlank()) return padrao;
        try { return LocalDate.parse(texto.trim()); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Data inválida: " + texto); }
    }

    private static Map<String, String> parametros(HttpExchange troca) {
        Map<String, String> mapa = new HashMap<>();
        String consulta = troca.getRequestURI().getRawQuery();
        if (consulta == null) return mapa;
        for (String par : consulta.split("&")) {
            if (par.isEmpty()) continue;
            int i = par.indexOf('=');
            mapa.put(URLDecoder.decode(i < 0 ? par : par.substring(0, i), StandardCharsets.UTF_8),
                    i < 0 ? "" : URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8));
        }
        return mapa;
    }

    private static <T> List<Object> lista(List<T> itens, Function<T, Object> f) {
        List<Object> saida = new ArrayList<>();
        for (T item : itens) saida.add(f.apply(item));
        return saida;
    }

    private void responder(HttpExchange troca, int status, Object corpo) throws IOException {
        byte[] bytes = Json.escrever(corpo).getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        troca.getResponseHeaders().set("Cache-Control", "no-store");
        troca.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) { saida.write(bytes); }
    }
}