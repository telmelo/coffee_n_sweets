package http;

import dao.PedidoDAO.OpcaoProduto;
import dto.ItemPedidoDTO;
import dto.PedidoResumoDTO;
import model.Cliente;
import model.Funcionario;
import model.Mesa;
import model.Pedido;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static http.Html.escapar;

public class PedidoView {
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    // ---------- lista ----------

    public static String lista(List<PedidoResumoDTO> pedidos, String statusFiltro, String info, String erro) {
        StringBuilder sb = new StringBuilder(estilo());
        sb.append(mensagens(info, erro));

        sb.append("<div class=\"barra-pedidos\">");
        sb.append("<form method=\"get\" action=\"/pedidos\" class=\"filtro-status\">");
        sb.append("<label for=\"status\">Status</label>");
        sb.append("<select id=\"status\" name=\"status\" onchange=\"this.form.submit()\">");
        sb.append(opcao("", "Todos", statusFiltro == null));
        for (String s : Pedido.STATUS) {
            sb.append(opcao(s, s, s.equals(statusFiltro)));
        }
        sb.append("</select></form>");

        sb.append("<div class=\"acoes\">");
        sb.append("<a class=\"botao botao-secundario\" href=\"/procedimentos\">Procedimentos</a>");
        sb.append("<a class=\"botao\" href=\"/pedidos/novo\">+ Novo pedido</a>");
        sb.append("</div></div>");

        sb.append("<div class=\"tabela-caixa\"><table><tr><th>Nº</th><th>Data</th><th>Cliente</th><th>Atendente</th>")
                .append("<th>Consumo</th><th>Pagamento</th><th>Status</th><th>Total</th><th>Ações</th></tr>");

        for (PedidoResumoDTO p : pedidos) {
            sb.append("<tr>");
            sb.append("<td>#").append(p.getIDPagamento()).append("</td>");
            sb.append("<td>").append(p.getData().format(DATA)).append(" ").append(p.getHorario().format(HORA)).append("</td>");
            sb.append("<td>").append(escapar(p.getCliente())).append("</td>");
            sb.append("<td>").append(escapar(p.getFuncionario())).append("</td>");
            sb.append("<td>").append(escapar(consumo(p))).append("</td>");
            sb.append("<td>").append(escapar(p.getFormaPagamento())).append("</td>");
            sb.append("<td>").append(escapar(p.getStatus())).append("</td>");
            sb.append("<td>").append(moeda(p.getValorTotal())).append("</td>");
            sb.append("<td><div class=\"acoes\">");
            sb.append("<a class=\"botao botao-pequeno\" href=\"/pedidos/ver?id=").append(p.getIDPagamento()).append("\">Ver</a>");
            sb.append("<a class=\"botao botao-pequeno botao-secundario\" href=\"/pedidos/editar?id=").append(p.getIDPagamento()).append("\">Editar</a>");
            sb.append(formExcluir(p.getIDPagamento(), "botao-pequeno botao-perigo"));
            sb.append("</div></td>");
            sb.append("</tr>");
        }
        if (pedidos.isEmpty()) {
            sb.append("<tr><td colspan=\"9\" class=\"vazio\">Nenhum pedido encontrado.</td></tr>");
        }
        sb.append("</table></div>");
        return sb.toString();
    }

    // ---------- formulário ----------

    public static String formulario(Pedido p, List<Cliente> clientes, List<Funcionario> funcionarios,
                                    List<Mesa> mesas, String mensagemErro) {
        StringBuilder sb = new StringBuilder(estilo());
        if (mensagemErro != null) {
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        sb.append("<form class=\"formulario\" method=\"post\" action=\"/pedidos/salvar\">");
        sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(p.getIDPagamento()).append("\">");

        sb.append("<div class=\"campo\"><label for=\"cliente\">Cliente</label><select id=\"cliente\" name=\"cliente\" required>");
        sb.append(opcao("", "Selecione…", p.getIDCliente() == 0));
        for (Cliente c : clientes) {
            sb.append(opcao(String.valueOf(c.getIDCliente()), c.getNome(), c.getIDCliente() == p.getIDCliente()));
        }
        sb.append("</select></div>");

        sb.append("<div class=\"campo\"><label for=\"funcionario\">Atendido por</label><select id=\"funcionario\" name=\"funcionario\" required>");
        sb.append(opcao("", "Selecione…", p.getIDFuncionario() == 0));
        for (Funcionario f : funcionarios) {
            sb.append(opcao(String.valueOf(f.getIDFuncionario()), f.getNome() + " (" + f.getCargo() + ")",
                    f.getIDFuncionario() == p.getIDFuncionario()));
        }
        sb.append("</select></div>");

        sb.append("<div class=\"campo\"><label for=\"tipo\">Tipo de consumo</label><select id=\"tipo\" name=\"tipo\">");
        for (String t : Pedido.TIPOS_CONSUMO) {
            sb.append(opcao(t, t, t.equals(p.getTipoConsumo())));
        }
        sb.append("</select></div>");

        sb.append("<div class=\"campo\"><label for=\"mesa\">Mesa</label><select id=\"mesa\" name=\"mesa\">");
        sb.append(opcao("", "Sem mesa", p.getNumeroMesa() == null));
        for (Mesa m : mesas) {
            boolean selecionada = p.getNumeroMesa() != null && p.getNumeroMesa() == m.getNumero();
            sb.append(opcao(String.valueOf(m.getNumero()),
                    "Mesa " + m.getNumero() + " (" + m.getLocalizacao() + ", " + m.getCapacidade() + " lugares)", selecionada));
        }
        sb.append("</select></div>");

        sb.append("<div class=\"campo\"><label for=\"forma\">Forma de pagamento</label><select id=\"forma\" name=\"forma\">");
        for (String f : Pedido.FORMAS_PAGAMENTO) {
            sb.append(opcao(f, f, f.equals(p.getFormaPagamento())));
        }
        sb.append("</select></div>");

        String cancelar = p.getIDPagamento() == 0 ? "/pedidos" : "/pedidos/ver?id=" + p.getIDPagamento();
        sb.append("<div class=\"formulario-acoes\"><button type=\"submit\">Salvar</button>");
        sb.append("<a class=\"botao botao-secundario\" href=\"").append(cancelar).append("\">Cancelar</a></div>");
        sb.append("</form>");

        // pedido para viagem não ocupa mesa: o select é desativado (e, sem ser enviado, a mesa fica nula)
        sb.append("<script>(function(){var t=document.getElementById('tipo'),m=document.getElementById('mesa');")
                .append("function a(){m.disabled=t.value==='Para viagem';if(m.disabled){m.value='';}}")
                .append("t.addEventListener('change',a);a();})();</script>");
        return sb.toString();
    }

    // ---------- detalhe com itens ----------

    public static String detalhe(PedidoResumoDTO p, List<ItemPedidoDTO> itens, List<OpcaoProduto> produtos,
                                 String info, String erro) {
        StringBuilder sb = new StringBuilder(estilo());
        sb.append(mensagens(info, erro));
        boolean pendente = "Pendente".equals(p.getStatus());

        sb.append("<div class=\"pedido-resumo\"><dl>");
        sb.append(item("Status", escapar(p.getStatus())));
        sb.append(item("Cliente", escapar(p.getCliente())));
        sb.append(item("Atendido por", escapar(p.getFuncionario())));
        sb.append(item("Consumo", escapar(consumo(p))));
        sb.append(item("Pagamento", escapar(p.getFormaPagamento())));
        sb.append(item("Data", p.getData().format(DATA) + " " + p.getHorario().format(HORA)));
        sb.append("</dl><div class=\"acoes\">");

        String proximo = proximoStatus(p.getStatus());
        if (proximo != null) {
            sb.append("<form method=\"post\" action=\"/pedidos/status\" class=\"form-inline\">")
                    .append("<input type=\"hidden\" name=\"id\" value=\"").append(p.getIDPagamento()).append("\">")
                    .append("<input type=\"hidden\" name=\"novo\" value=\"").append(escapar(proximo)).append("\">")
                    .append("<button type=\"submit\">Avançar para ").append(escapar(proximo)).append("</button></form>");
        }
        sb.append("<a class=\"botao botao-secundario\" href=\"/pedidos/editar?id=").append(p.getIDPagamento()).append("\">Editar</a>");
        sb.append(formExcluir(p.getIDPagamento(), "botao-perigo"));
        sb.append("<a class=\"botao botao-secundario\" href=\"/pedidos\">Voltar</a>");
        sb.append("</div></div>");

        sb.append("<h2 class=\"titulo-bloco\">Itens do pedido #").append(p.getIDPagamento()).append("</h2>");
        sb.append("<div class=\"tabela-caixa corpo-bloco\"><table><tr><th>Item</th><th>Produto</th><th>Qtd</th>")
                .append("<th>Preço unit.</th><th>Subtotal</th><th>Observação</th>")
                .append(pendente ? "<th>Ações</th>" : "").append("</tr>");

        double soma = 0;
        for (ItemPedidoDTO i : itens) {
            soma += i.getSubtotal();
            sb.append("<tr>");
            sb.append("<td>").append(i.getNumeroItem()).append("</td>");
            sb.append("<td>").append(escapar(i.getProduto())).append("</td>");
            sb.append("<td>").append(i.getQuantidade()).append("</td>");
            sb.append("<td>").append(moeda(i.getPrecoUnitario())).append("</td>");
            sb.append("<td>").append(moeda(i.getSubtotal())).append("</td>");
            sb.append("<td>").append(i.getObservacao() == null || i.getObservacao().isBlank() ? "—" : escapar(i.getObservacao())).append("</td>");
            if (pendente) {
                sb.append("<td><form method=\"post\" action=\"/pedidos/itens/remover\" class=\"form-inline\" ")
                        .append("onsubmit=\"return confirm('Remover este item?')\">")
                        .append("<input type=\"hidden\" name=\"pedido\" value=\"").append(p.getIDPagamento()).append("\">")
                        .append("<input type=\"hidden\" name=\"item\" value=\"").append(i.getNumeroItem()).append("\">")
                        .append("<button type=\"submit\" class=\"botao-pequeno botao-perigo\">Remover</button></form></td>");
            }
            sb.append("</tr>");
        }
        int colunas = pendente ? 7 : 6;
        if (itens.isEmpty()) {
            sb.append("<tr><td colspan=\"").append(colunas).append("\" class=\"vazio\">Este pedido ainda não tem itens.</td></tr>");
        }
        sb.append("<tr class=\"linha-total\"><td colspan=\"4\">Total do pedido (calculado pelo trigger)</td><td colspan=\"")
                .append(colunas - 4).append("\">").append(moeda(p.getValorTotal())).append("</td></tr>");
        sb.append("</table></div>");

        // se o total do banco não bate com a soma dos itens, avisa (ex.: dados inseridos antes do trigger existir)
        if (Math.abs(soma - p.getValorTotal()) > 0.005) {
            sb.append("<p class=\"nota-total\">Atenção: a soma dos itens (").append(moeda(soma))
                    .append(") é diferente do total registrado no banco. Confira se os triggers de Item_pedido estão criados.</p>");
        }

        if (pendente) {
            sb.append("<h2 class=\"secao-item-titulo\">Adicionar item</h2>");
            sb.append("<form class=\"formulario formulario-item\" method=\"post\" action=\"/pedidos/itens/adicionar\">");
            sb.append("<input type=\"hidden\" name=\"pedido\" value=\"").append(p.getIDPagamento()).append("\">");
            sb.append("<div class=\"campo\"><label for=\"produto\">Produto</label><select id=\"produto\" name=\"produto\" required>");
            sb.append(opcao("", "Selecione…", true));
            for (OpcaoProduto o : produtos) {
                sb.append(opcao(String.valueOf(o.id()), o.nome() + " — " + moeda(o.preco()), false));
            }
            sb.append("</select></div>");
            sb.append("<div class=\"campo\"><label for=\"quantidade\">Quantidade</label>")
                    .append("<input id=\"quantidade\" type=\"number\" name=\"quantidade\" min=\"1\" max=\"999\" value=\"1\" required></div>");
            sb.append("<div class=\"campo\"><label for=\"observacao\">Observação (opcional)</label>")
                    .append("<input id=\"observacao\" type=\"text\" name=\"observacao\" maxlength=\"200\"></div>");
            sb.append("<div class=\"formulario-acoes\"><button type=\"submit\">Adicionar item</button></div></form>");
        } else {
            sb.append("<p class=\"nota-total\">Os itens só podem ser alterados enquanto o pedido está pendente.</p>");
        }
        return sb.toString();
    }

    // ---------- auxiliares ----------

    private static String proximoStatus(String status) {
        return switch (status) {
            case "Pendente" -> "Em preparo";
            case "Em preparo" -> "Pronto";
            default -> null;
        };
    }

    private static String consumo(PedidoResumoDTO p) {
        if ("Para viagem".equals(p.getTipoConsumo())) return "Para viagem";
        return p.getNumeroMesa() == null ? "Aqui (sem mesa)" : "Aqui, mesa " + p.getNumeroMesa();
    }

    private static String item(String rotulo, String valorHtml) {
        return "<div><dt>" + escapar(rotulo) + "</dt><dd>" + valorHtml + "</dd></div>";
    }

    private static String opcao(String valor, String rotulo, boolean selecionada) {
        return "<option value=\"" + escapar(valor) + "\"" + (selecionada ? " selected" : "") + ">" + escapar(rotulo) + "</option>";
    }

    private static String formExcluir(int id, String classes) {
        return "<form method=\"post\" action=\"/pedidos/excluir\" class=\"form-inline\" "
                + "onsubmit=\"return confirm('Excluir este pedido e todos os itens dele?')\">"
                + "<input type=\"hidden\" name=\"id\" value=\"" + id + "\">"
                + "<button type=\"submit\" class=\"" + classes + "\">Excluir</button></form>";
    }

    private static String mensagens(String info, String erro) {
        StringBuilder sb = new StringBuilder();
        if (info != null && !info.isBlank()) sb.append("<div class=\"info\">").append(escapar(info)).append("</div>");
        if (erro != null && !erro.isBlank()) sb.append("<div class=\"alerta\">").append(escapar(erro)).append("</div>");
        return sb.toString();
    }

    private static String moeda(double valor) {
        return String.format(BR, "R$ %,.2f", valor);
    }

    private static String estilo() {
        return """
                <style>
                .barra-pedidos{display:flex;flex-wrap:wrap;justify-content:space-between;align-items:flex-end;gap:14px;margin-bottom:22px}
                .filtro-status{display:flex;flex-direction:column;gap:4px;min-width:170px}
                .secao-item-titulo{margin-top:34px;margin-bottom:14px}
                .formulario-item{margin-top:8px}
                .pedido-resumo{background:#fff;border:2px solid var(--borda);border-radius:var(--raio);box-shadow:var(--sombra);
                  padding:20px 24px;margin-bottom:28px}
                .titulo-bloco{margin-top:34px;margin-bottom:14px}
                .corpo-bloco{margin-top:8px}
                .filtro-status label{font-weight:700;color:var(--framboesa);font-size:.85rem}
                .info{margin-bottom:22px;padding:14px 18px;border:2px solid #b9e2c8;border-radius:16px;background:#effaf3;
                  color:#1f7a4a;font-weight:700}
                .info::before{content:"✓ "}
                .pedido-resumo{background:#fff;border:2px solid var(--borda);border-radius:var(--raio);box-shadow:var(--sombra);
                  padding:20px 24px;margin-bottom:8px}
                .pedido-resumo dl{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:14px;margin-bottom:18px}
                .pedido-resumo dt{font-size:.8rem;font-weight:700;color:var(--texto-suave);text-transform:uppercase;letter-spacing:.06em}
                .pedido-resumo dd{margin:0;font-weight:600}
                .linha-total td{font-weight:700;background:var(--rosa-claro)}
                .nota-total{margin-top:10px;color:var(--texto-suave)}
                td.vazio{text-align:center;color:var(--texto-suave)}
                </style>
                """;
    }
}