package http;

import dto.*;

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

    public static String pagina(List<FaturamentoDTO> faturamento,
                                List<TopProdutosDTO> topProdutos,
                                List<BaristasDestaqueDTO> baristas,
                                List<CafesEGraosDTO> cafes,
                                List<ProdutosNaoVendidosDTO> naoVendidos,
                                List<RelacaoMesaPedidoDTO> mesaPedido,
                                List<ClienteUltimoPedidoDTO> ultimoPedido,
                                List<ClienteCafeSemLancheDTO> cafeSemLanche,
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

        sb.append(secaoProdutosNaoVendidos(naoVendidos));
        sb.append(secaoMesaPedido(mesaPedido));
        sb.append(secaoUltimoPedido(ultimoPedido));
        sb.append(secaoCafeSemLanche(cafeSemLanche));
        return sb.toString();
    }

    private static String secaoFaturamento(List<FaturamentoDTO> faturamento, boolean python) {
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

    private static String secaoProdutosNaoVendidos(List<ProdutosNaoVendidosDTO> produtos) {
        String conteudo;
        if (produtos.isEmpty()) {
            conteudo = vazio("Todos os produtos já foram vendidos pelo menos uma vez.");
        } else {
            StringBuilder tabela = new StringBuilder("<table><tr><th>Produto</th><th>Preço</th></tr>");
            for (ProdutosNaoVendidosDTO p : produtos) {
                tabela.append("<tr><td>").append(escapar(p.getNome())).append("</td>")
                        .append("<td>").append(moeda(p.getPreco())).append("</td></tr>");
            }
            tabela.append("</table>");
            conteudo = detalhes("Ver tabela (" + produtos.size() + " produtos)", tabela.toString());
        }

        return "<div class=\"secao\">"
                + "<h2>Produtos nunca vendidos</h2>"
                + "<p class=\"consulta\">Consulta: LEFT JOIN (Produto × Item_pedido) filtrando IS NULL</p>"
                + conteudo
                + "</div>";
    }

    private static String secaoMesaPedido(List<RelacaoMesaPedidoDTO> linhas) {
        String conteudo;
        if (linhas.isEmpty()) {
            conteudo = vazio("Nenhuma mesa ou pedido encontrado.");
        } else {
            StringBuilder tabela = new StringBuilder(
                    "<table><tr><th>Mesa</th><th>Localização</th><th>Pedido</th><th>Data</th><th>Valor total</th></tr>");
            for (RelacaoMesaPedidoDTO r : linhas) {
                // pedido sem mesa e mesa sem pedido chegam com campos nulos
                String mesa = r.getNumeroMesa() == null ? "Sem mesa" : String.valueOf(r.getNumeroMesa());
                String localizacao = r.getLocalizacao() == null ? "—" : r.getLocalizacao();
                String pedido = r.getIDPagamento() == null ? "Sem pedidos" : "#" + r.getIDPagamento();
                String data = r.getData() == null ? "—" : r.getData().toString();
                String valor = r.getValorTotal() == null ? "—" : moeda(r.getValorTotal());

                tabela.append("<tr>")
                        .append("<td>").append(escapar(mesa)).append("</td>")
                        .append("<td>").append(escapar(localizacao)).append("</td>")
                        .append("<td>").append(escapar(pedido)).append("</td>")
                        .append("<td>").append(data).append("</td>")
                        .append("<td>").append(valor).append("</td>")
                        .append("</tr>");
            }
            tabela.append("</table>");
            conteudo = detalhes("Ver tabela", tabela.toString());
        }

        return "<div class=\"secao\">"
                + "<h2>Mesas e seus pedidos</h2>"
                + "<p class=\"consulta\">Consulta: LEFT JOIN + RIGHT JOIN com UNION (Mesa × Pedido_pagamento), incluindo mesas sem pedidos e pedidos sem mesa</p>"
                + conteudo
                + "</div>";
    }

    private static String secaoUltimoPedido(List<ClienteUltimoPedidoDTO> clientes) {
        String conteudo;
        if (clientes.isEmpty()) {
            conteudo = vazio("Nenhum cliente com pedidos registrados.");
        } else {
            StringBuilder tabela = new StringBuilder(
                    "<table><tr><th>Cliente</th><th>Último pedido</th><th>Data</th><th>Pagamento</th><th>Dias sem comprar</th></tr>");
            for (ClienteUltimoPedidoDTO c : clientes) {
                tabela.append("<tr>")
                        .append("<td>").append(escapar(c.getNome())).append("</td>")
                        .append("<td>#").append(c.getIDPagamento()).append("</td>")
                        .append("<td>").append(c.getDataUltimoPedido()).append("</td>")
                        .append("<td>").append(escapar(c.getFormaPagamento())).append("</td>")
                        .append("<td>").append(c.getDiasSemComprar()).append("</td>")
                        .append("</tr>");
            }
            tabela.append("</table>");
            conteudo = detalhes("Ver tabela (" + clientes.size() + " registros)", tabela.toString());
        }

        return "<div class=\"secao\">"
                + "<h2>Último pedido de cada cliente</h2>"
                + "<p class=\"consulta\">Consulta: JOIN (Pedido_pagamento × Cliente) + subconsulta correlacionada com MAX(data) + DATEDIFF</p>"
                + conteudo
                + "</div>";
    }

    private static String secaoCafeSemLanche(List<ClienteCafeSemLancheDTO> clientes) {
        String conteudo;
        if (clientes.isEmpty()) {
            conteudo = vazio("Nenhum cliente comprou café sem nunca ter comprado lanche.");
        } else {
            StringBuilder tabela = new StringBuilder("<table><tr><th>Cliente</th><th>E-mail</th></tr>");
            for (ClienteCafeSemLancheDTO c : clientes) {
                tabela.append("<tr><td>").append(escapar(c.getNome())).append("</td>")
                        .append("<td>").append(escapar(c.getEmail())).append("</td></tr>");
            }
            tabela.append("</table>");
            conteudo = detalhes("Ver tabela (" + clientes.size() + " clientes)", tabela.toString());
        }

        return "<div class=\"secao\">"
                + "<h2>Clientes que compram café e nunca compraram lanche</h2>"
                + "<p class=\"consulta\">Consulta: EXISTS (pedidos com Cafe) + NOT EXISTS (pedidos com Lanche)</p>"
                + conteudo
                + "</div>";
    }

    private static String grafico(boolean python, String arquivoPng, String descricao, Supplier<String> svg) {
        if (python) {
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

    private static String vazio(String mensagem) {
        return "<p>" + escapar(mensagem) + "</p>";
    }

    private static String moeda(double valor) {
        return String.format(BR, "R$ %,.2f", valor);
    }

    private static String estilo() {
        return "<style>"
                + ".cartoes{display:flex;gap:16px;flex-wrap:wrap;margin-bottom:28px}"
                + ".cartao{border:1px solid #ddd;border-radius:8px;padding:12px 20px;min-width:170px}"
                + ".cartao .valor{font-size:1.6em;font-weight:bold;color:#c2306a}"
                + ".secao{margin-bottom:40px}"
                + ".consulta{color:#666;font-size:.9em;margin-top:-8px}"
                + "details{margin-top:12px}"
                + "summary{cursor:pointer;color:#c2306a}"
                + "</style>";
    }
}