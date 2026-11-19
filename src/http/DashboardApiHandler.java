package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ClienteDAO;
import dao.ConsultasDAO;
import dao.DashboardDAO;
import dao.DashboardDAO.PedidoLinha;
import dao.EstatisticasDAO;
import dao.LogPrecoDAO;
import dao.ProdutoDAO;
import dto.TopProdutosDTO;
import util.Resposta;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Endpoints JSON do dashboard:
 *   GET /api/dashboard?inicio=AAAA-MM-DD&fim=AAAA-MM-DD&forma=Crédito   todos os dados dos gráficos e tabelas
 *   GET /api/boxplot?inicio=...&fim=...&forma=...                       gera o PNG do boxplot (Python)
 */
public class DashboardApiHandler implements HttpHandler {
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final ConsultasDAO consultasDAO = new ConsultasDAO();
    private final EstatisticasDAO estatisticasDAO = new EstatisticasDAO();
    private final LogPrecoDAO logPrecoDAO = new LogPrecoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    private record Filtro(LocalDate inicio, LocalDate fim, String forma, LocalDate minimo, LocalDate maximo) {}

    @Override
    public void handle(HttpExchange troca) throws IOException {
        if (!troca.getRequestMethod().equals("GET")) {
            Resposta.naoEncontrado(troca);
            return;
        }

        try {
            switch (troca.getRequestURI().getPath()) {
                case "/api/dashboard" -> dashboard(troca);
                case "/api/boxplot" -> boxplot(troca);
                default -> Resposta.naoEncontrado(troca);
            }
        } catch (IllegalArgumentException e) {
            responder(troca, 400, Json.obj("erro", e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            responder(troca, 500, Json.obj("erro", "Erro no banco de dados: " + e.getMessage()));
        }
    }

    private void dashboard(HttpExchange troca) throws IOException, SQLException {
        Filtro f = lerFiltro(troca);

        // partes que obedecem ao filtro de período e forma de pagamento
        List<PedidoLinha> pedidos = dashboardDAO.pedidos(f.inicio(), f.fim(), f.forma());
        List<TopProdutosDTO> top = dashboardDAO.topProdutos(f.inicio(), f.fim(), f.forma());

        // partes que mostram o retrato completo do banco
        var naoVendidos = consultasDAO.produtosNaoVendidos();
        var mesas = consultasDAO.relacaoMesaPedido();
        var ultimoPedido = consultasDAO.clientesUltimoPedido();
        var cafeSemLanche = consultasDAO.clientesCafeSemLanche();
        var baristas = consultasDAO.baristasAcimaDaMedia();
        var cafes = consultasDAO.cafesEGraos();
        var categorias = estatisticasDAO.distribuicaoCategorias();
        var logsPreco = logPrecoDAO.listarAlteracoesPreco();

        int totalClientes = clienteDAO.listar().size();
        int totalProdutos = produtoDAO.listar().size();

        Map<String, Object> resposta = Json.obj(
                "filtro", Json.obj(
                        "inicio", f.inicio(), "fim", f.fim(), "forma", f.forma(),
                        "minimo", f.minimo(), "maximo", f.maximo(),
                        "formas", DashboardDAO.FORMAS_PAGAMENTO),
                "kpis", kpis(pedidos, totalClientes, totalProdutos),
                "faturamentoDia", faturamentoDia(pedidos),
                "histograma", histograma(pedidos),
                "topProdutos", lista(top, t -> Json.obj("nome", t.getNome(), "total", t.getTotalVendido())),
                "totalProdutos", totalProdutos,
                "naoVendidos", lista(naoVendidos, p -> Json.obj("nome", p.getNome(), "preco", p.getPreco())),
                "mesas", lista(mesas, m -> Json.obj(
                        "mesa", m.getNumeroMesa(), "localizacao", m.getLocalizacao(),
                        "pedido", m.getIDPagamento(), "data", m.getData(), "valor", m.getValorTotal())),
                "ultimoPedido", lista(ultimoPedido, c -> Json.obj(
                        "cliente", c.getNome(), "pedido", c.getIDPagamento(), "data", c.getDataUltimoPedido(),
                        "forma", c.getFormaPagamento(), "dias", c.getDiasSemComprar())),
                "cafeSemLanche", lista(cafeSemLanche, c -> Json.obj(
                        "nome", c.getNome(), "email", c.getEmail())),
                "baristas", lista(baristas, b -> Json.obj("nome", b.getNome(), "itens", b.getItensPreparados())),
                "cafes", lista(cafes, c -> Json.obj(
                        "cafe", c.getNomeCafe(), "grao", c.getNomeGrao(), "origem", c.getOrigem(),
                        "fornecedor", c.getRazaoSocial(), "precoKg", c.getPrecoKG())),
                "categorias", lista(categorias, c -> Json.obj("categoria", c.getCategoria(), "quantidade", c.getQuantidade())),
                "logsPreco", lista(logsPreco, l -> Json.obj(
                        "produto", l.getNomeProduto(), "de", l.getPrecoAntigo(), "para", l.getPrecoNovo(),
                        "quando", l.getDataAlteracao(), "usuario", l.getUsuario()))
        );
        responder(troca, 200, resposta);
    }

    private void boxplot(HttpExchange troca) throws IOException, SQLException {
        Filtro f = lerFiltro(troca);
        List<PedidoLinha> pedidos = dashboardDAO.pedidos(f.inicio(), f.fim(), f.forma());

        boolean ok = GraficosPython.gerarBoxplot(pedidos);
        if (ok) {
            responder(troca, 200, Json.obj("ok", true));
        } else {
            responder(troca, 503, Json.obj("ok", false,
                    "erro", "Não foi possível gerar o boxplot: verifique se o Python e o matplotlib estão instalados."));
        }
    }

    // ---------- filtro ----------

    private Filtro lerFiltro(HttpExchange troca) throws SQLException {
        Map<String, String> q = parametros(troca);
        LocalDate[] intervalo = dashboardDAO.intervaloDatas();

        LocalDate inicio = data(q.get("inicio"), intervalo[0]);
        LocalDate fim = data(q.get("fim"), intervalo[1]);
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data inicial não pode ser depois da data final.");
        }

        String forma = q.get("forma");
        if (forma == null || forma.isBlank()) {
            forma = null;
        } else if (!DashboardDAO.FORMAS_PAGAMENTO.contains(forma)) {
            throw new IllegalArgumentException("Forma de pagamento inválida.");
        }
        return new Filtro(inicio, fim, forma, intervalo[0], intervalo[1]);
    }

    private static LocalDate data(String texto, LocalDate padrao) {
        if (texto == null || texto.isBlank()) return padrao;
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida: " + texto);
        }
    }

    private static Map<String, String> parametros(HttpExchange troca) {
        Map<String, String> mapa = new HashMap<>();
        String consulta = troca.getRequestURI().getRawQuery();
        if (consulta == null) return mapa;
        for (String par : consulta.split("&")) {
            if (par.isEmpty()) continue;
            int i = par.indexOf('=');
            String chave = URLDecoder.decode(i < 0 ? par : par.substring(0, i), StandardCharsets.UTF_8);
            String valor = i < 0 ? "" : URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8);
            mapa.put(chave, valor);
        }
        return mapa;
    }

    // ---------- estatísticas calculadas sobre os pedidos do período ----------

    private static Map<String, Object> kpis(List<PedidoLinha> pedidos, int clientes, int produtos) {
        int n = pedidos.size();
        double[] valores = pedidos.stream().mapToDouble(PedidoLinha::valor).sorted().toArray();
        double soma = Arrays.stream(valores).sum();
        double media = n == 0 ? 0 : soma / n;

        double mediana = 0;
        if (n > 0) {
            mediana = n % 2 == 1 ? valores[n / 2] : (valores[n / 2 - 1] + valores[n / 2]) / 2;
        }

        // variância e desvio padrão populacionais (mesma definição de VAR_POP e STDDEV_POP do MySQL)
        double variancia = 0;
        if (n > 0) {
            double acumulado = 0;
            for (double v : valores) acumulado += (v - media) * (v - media);
            variancia = acumulado / n;
        }

        // moda: forma de pagamento mais usada (em empate, a primeira em ordem alfabética)
        Map<String, Integer> porForma = new TreeMap<>();
        int viagem = 0;
        for (PedidoLinha p : pedidos) {
            porForma.merge(p.forma(), 1, Integer::sum);
            if ("Para viagem".equals(p.tipoConsumo())) viagem++;
        }
        String moda = null;
        int maior = 0;
        for (Map.Entry<String, Integer> e : porForma.entrySet()) {
            if (e.getValue() > maior) {
                maior = e.getValue();
                moda = e.getKey();
            }
        }

        return Json.obj(
                "pedidos", n,
                "faturamento", soma,
                "media", media,
                "mediana", mediana,
                "moda", moda,
                "variancia", variancia,
                "desvio", Math.sqrt(variancia),
                "viagemPct", n == 0 ? 0.0 : viagem * 100.0 / n,
                "clientes", clientes,
                "produtos", produtos);
    }

    // soma e quantidade de pedidos por (dia, forma de pagamento)
    private static List<Object> faturamentoDia(List<PedidoLinha> pedidos) {
        Map<String, double[]> grupos = new TreeMap<>();
        for (PedidoLinha p : pedidos) {
            double[] g = grupos.computeIfAbsent(p.data() + "|" + p.forma(), k -> new double[2]);
            g[0] += p.valor();
            g[1]++;
        }

        List<Object> saida = new ArrayList<>();
        for (Map.Entry<String, double[]> e : grupos.entrySet()) {
            String[] chave = e.getKey().split("\\|");
            saida.add(Json.obj(
                    "data", chave[0],
                    "forma", chave[1],
                    "valor", Math.round(e.getValue()[0] * 100) / 100.0,
                    "pedidos", (int) e.getValue()[1]));
        }
        return saida;
    }

    // faixas de valor em múltiplos de R$ 5, com cerca de 6 faixas
    private static List<Object> histograma(List<PedidoLinha> pedidos) {
        List<Object> saida = new ArrayList<>();
        if (pedidos.isEmpty()) return saida;

        double max = pedidos.stream().mapToDouble(PedidoLinha::valor).max().orElse(0);
        int passo = 5 * Math.max(1, (int) Math.ceil(max / 6 / 5));
        int faixas = (int) Math.floor(max / passo) + 1;

        int[] contagem = new int[faixas];
        for (PedidoLinha p : pedidos) {
            contagem[(int) Math.floor(p.valor() / passo)]++;
        }
        for (int i = 0; i < faixas; i++) {
            saida.add(Json.obj("faixa", "R$ " + (i * passo) + "–" + ((i + 1) * passo), "quantidade", contagem[i]));
        }
        return saida;
    }

    private static <T> List<Object> lista(List<T> itens, Function<T, Object> conversor) {
        List<Object> saida = new ArrayList<>();
        for (T item : itens) saida.add(conversor.apply(item));
        return saida;
    }

    private void responder(HttpExchange troca, int status, Object corpo) throws IOException {
        byte[] bytes = Json.escrever(corpo).getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        troca.getResponseHeaders().set("Cache-Control", "no-store");
        troca.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }
}