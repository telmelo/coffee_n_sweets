package http;

import dto.BaristasDestaqueDTO;
import dto.CafesEGraosDTO;
import dto.FaturamentoDTO;
import dto.TopProdutosDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static http.Html.escapar;

public class DashboardView {
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    /**
     * @param graficosPython true se o script Python gerou as imagens PNG;
     *                       false usa os gráficos SVG feitos em Java como alternativa
     */
    public static String pagina(List<FaturamentoDTO> faturamento,
                                List<TopProdutosDTO> topProdutos,
                                List<BaristasDestaqueDTO> baristas,
                                List<CafesEGraosDTO> cafes,
                                int totalClientes,
                                int totalProdutos,
                                boolean graficosPython) {
        StringBuilder sb = new StringBuilder();
        sb.append(estilo());

        double faturamentoTotal = faturamento.stream().mapToDouble(FaturamentoDTO::getFaturamento).sum();

        sb.append("<div class=\"cartoes\">");
        sb.append(cartao("Faturamento total", moeda(faturamentoTotal)));
        sb.append(cartao("Clientes cadastrados", String.valueOf(totalClientes)));
        sb.append(cartao("Produtos no cardápio", String.valueOf(totalProdutos)));
        sb.append("</div>");

        sb.append(secaoFaturamento(faturamento, graficosPython));
        sb.append(secaoTopProdutos(topProdutos, graficosPython));
        sb.append(secaoBaristas(baristas, graficosPython));
        sb.append(secaoCafes(cafes));
        return sb.toString();
    }

    // ---------- Consulta 1: faturamento por dia e forma de pagamento ----------
    private static String secaoFaturamento(List<FaturamentoDTO> faturamento, boolean python) {
        // agrega os resultados da consulta para montar os gráficos SVG (alternativa ao Python)
        Map<String, Double> porForma = new TreeMap<>();
        Map<LocalDate, Double> porDia = new TreeMap<>();
        for (FaturamentoDTO f : faturamento) {
            porForma.merge(f.getFormaPagamento(), f.getFaturamento(), Double::sum);
            porDia.merge(f.getData(), f.getFaturamento(), Double::sum);
        }

        List<String> rotulosDia = new ArrayList<>();
        for (LocalDate dia : porDia.keySet()) rotulosDia.add(dia.format(DIA_MES));

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"secao\">");
        sb.append("<h2>Faturamento por dia e forma de pagamento</h2>");
        sb.append("<p class=\"consulta\">Consulta: GROUP BY (data, forma de pagamento) + SUM</p>");

        sb.append("<h3>")
                .append(python ? "Distribuição do faturamento diário por forma de pagamento" : "Faturamento por forma de pagamento")
                .append("</h3>");
        sb.append(grafico(python, "faturamento_boxplot.png", "Distribuição do faturamento diário por forma de pagamento",
                () -> Graficos.barrasVerticais(new ArrayList<>(porForma.keySet()),
                        new ArrayList<>(porForma.values()), DashboardView::moeda)));

        sb.append("<h3>Faturamento diário</h3>");
        sb.append(grafico(python, "faturamento_diario.png", "Faturamento diário",
                () -> Graficos.linha(rotulosDia, new ArrayList<>(porDia.values()), DashboardView::moeda)));

        List<FaturamentoDTO> ordenada = new ArrayList<>(faturamento);
        ordenada.sort(Comparator.comparing(FaturamentoDTO::getData)
                .thenComparing(FaturamentoDTO::getFormaPagamento));

        StringBuilder tabela = new StringBuilder("<table><tr><th>Data</th><th>Forma de pagamento</th><th>Faturamento</th></tr>");
        for (FaturamentoDTO f : ordenada) {
            tabela.append("<tr><td>").append(f.getData()).append("</td>")
                    .append("<td>").append(escapar(f.getFormaPagamento())).append("</td>")
                    .append("<td>").append(moeda(f.getFaturamento())).append("</td></tr>");
        }
        tabela.append("</table>");
        sb.append(detalhes("Ver tabela completa", tabela.toString()));

        sb.append("</div>");
        return sb.toString();
    }

    // ---------- Consulta 2: top 10 produtos mais vendidos ----------
    private static String secaoTopProdutos(List<TopProdutosDTO> topProdutos, boolean python) {
        List<String> rotulos = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        for (TopProdutosDTO p : topProdutos) {
            rotulos.add(p.getNome());
            valores.add((double) p.getTotalVendido());
        }

        StringBuilder tabela = new StringBuilder("<table><tr><th>#</th><th>Produto</th><th>Unidades vendidas</th></tr>");
        int posicao = 1;
        for (TopProdutosDTO p : topProdutos) {
            tabela.append("<tr><td>").append(posicao++).append("</td>")
                    .append("<td>").append(escapar(p.getNome())).append("</td>")
                    .append("<td>").append(p.getTotalVendido()).append("</td></tr>");
        }
        tabela.append("</table>");

        return "<div class=\"secao\">"
                + "<h2>Os 10 produtos mais vendidos</h2>"
                + "<p class=\"consulta\">Consulta: JOIN (Item_pedido × Produto) + SUM + GROUP BY + ORDER BY + LIMIT 10</p>"
                + grafico(python, "top_produtos.png", "Os 10 produtos mais vendidos",
                () -> Graficos.barrasHorizontais(rotulos, valores, v -> String.valueOf((int) v)))
                + detalhes("Ver tabela", tabela.toString())
                + "</div>";
    }

    // ---------- Consulta 3: baristas acima da média ----------
    private static String secaoBaristas(List<BaristasDestaqueDTO> baristas, boolean python) {
        List<String> rotulos = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        for (BaristasDestaqueDTO b : baristas) {
            rotulos.add(b.getNome());
            valores.add((double) b.getItensPreparados());
        }

        StringBuilder tabela = new StringBuilder("<table><tr><th>Barista</th><th>Itens preparados</th></tr>");
        for (BaristasDestaqueDTO b : baristas) {
            tabela.append("<tr><td>").append(escapar(b.getNome())).append("</td>")
                    .append("<td>").append(b.getItensPreparados()).append("</td></tr>");
        }
        tabela.append("</table>");

        return "<div class=\"secao\">"
                + "<h2>Baristas acima da média de produtividade</h2>"
                + "<p class=\"consulta\">Consulta: JOIN (Prepara × Funcionario) + GROUP BY + HAVING com subconsulta (média de itens por barista)</p>"
                + grafico(python, "baristas.png", "Baristas acima da média de produtividade",
                () -> Graficos.barrasHorizontais(rotulos, valores, v -> String.valueOf((int) v)))
                + detalhes("Ver tabela", tabela.toString())
                + "</div>";
    }

    // ---------- Consulta 4: cafés, grãos e fornecedores ----------
    private static String secaoCafes(List<CafesEGraosDTO> cafes) {
        StringBuilder tabela = new StringBuilder(
                "<table><tr><th>Café</th><th>Grão</th><th>Origem</th><th>Fornecedor</th><th>Preço/kg</th></tr>");
        for (CafesEGraosDTO c : cafes) {
            tabela.append("<tr>")
                    .append("<td>").append(escapar(c.getNomeCafe())).append("</td>")
                    .append("<td>").append(escapar(c.getNomeGrao())).append("</td>")
                    .append("<td>").append(escapar(c.getOrigem())).append("</td>")
                    .append("<td>").append(escapar(c.getRazaoSocial())).append("</td>")
                    .append("<td>").append(moeda(c.getPrecoKG())).append("</td>")
                    .append("</tr>");
        }
        tabela.append("</table>");

        return "<div class=\"secao\">"
                + "<h2>Grãos de cada café e seus fornecedores</h2>"
                + "<p class=\"consulta\">Consulta: JOIN entre Cafe, Grao, Fornece, Fornecedor e Produto</p>"
                + detalhes("Ver tabela", tabela.toString())
                + "</div>";
    }

    // ---------- auxiliares ----------

    /** Mostra a imagem gerada pelo Python ou, se não houver, o gráfico SVG feito em Java. */
    private static String grafico(boolean python, String arquivoPng, String descricao, Supplier<String> svg) {
        if (python) {
            // o parâmetro ?v= evita que o navegador mostre uma versão antiga em cache
            return "<img src=\"/graficos/" + arquivoPng + "?v=" + System.currentTimeMillis()
                    + "\" alt=\"" + escapar(descricao) + "\" style=\"max-width:100%\">";
        }
        return svg.get();
    }

    private static String cartao(String titulo, String valor) {
        return "<div class=\"cartao\"><div>" + escapar(titulo) + "</div><div class=\"valor\">"
                + escapar(valor) + "</div></div>";
    }

    private static String detalhes(String resumo, String conteudo) {
        return "<details open><summary>" + escapar(resumo) + "</summary>" + conteudo + "</details>";
    }

    private static String moeda(double valor) {
        return String.format(BR, "R$ %,.2f", valor);
    }

    private static String estilo() {
        return "<style>"
                + ".cartoes{display:flex;gap:16px;flex-wrap:wrap;margin-bottom:28px}"
                + ".cartao{border:1px solid #ddd;border-radius:8px;padding:12px 20px;min-width:170px}"
                + ".cartao .valor{font-size:1.6em;font-weight:bold;color:#6f4e37}"
                + ".secao{margin-bottom:40px}"
                + ".consulta{color:#666;font-size:.9em;margin-top:-8px}"
                + "details{margin-top:12px}"
                + "summary{cursor:pointer;color:#6f4e37}"
                + "</style>";
    }
}