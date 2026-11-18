package http;

import dto.PedidoStatusDTO;
import dto.PreparoDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static http.Html.escapar;

public class ProcedimentosView {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static String moeda(double valor) {
        return String.format(Locale.of("pt", "BR"), "R$ %.2f", valor);
    }

    public static String pagina(String mensagem, boolean erro, List<PedidoStatusDTO> pedidos,
                                List<PreparoDTO> preparos) {
        StringBuilder sb = new StringBuilder();

        if (mensagem != null && !mensagem.isBlank()) {
            sb.append("<div class=\"").append(erro ? "alerta" : "sucesso").append("\">")
                    .append(escapar(mensagem)).append("</div>");
        }

        sb.append("<section class=\"secao\">");
        sb.append("<h2>Atualizar status de um pedido</h2>");
        sb.append("<p class=\"descricao\">Procedimento <code>sp_atualizar_status_pedido</code>: ")
                .append("o status só avança na ordem Pendente → Em preparo → Pronto. ")
                .append("Qualquer outra mudança é recusada.</p>");
        sb.append("<form class=\"form-linha\" method=\"post\" action=\"/procedimentos/status\">");
        sb.append("<div class=\"campo\"><label for=\"pedido\">Pedido</label><select id=\"pedido\" name=\"pedido\">");
        for (PedidoStatusDTO p : pedidos) {
            sb.append("<option value=\"").append(p.idPagamento()).append("\">#").append(p.idPagamento())
                    .append(" · ").append(escapar(p.status())).append("</option>");
        }
        sb.append("</select></div>");
        sb.append("<div class=\"campo\"><label for=\"novo\">Novo status</label><select id=\"novo\" name=\"novo\">")
                .append("<option>Pendente</option><option>Em preparo</option><option>Pronto</option></select></div>");
        sb.append("<button type=\"submit\">Atualizar status</button></form>");
        sb.append("</section>");

        sb.append("<section class=\"secao\">");
        sb.append("<h2>Distribuir itens pendentes</h2>");
        sb.append("<p class=\"descricao\">Procedimento <code>sp_distribuir_itens_pendentes</code> (usa cursor): ")
                .append("percorre os itens de pedidos pendentes que ainda não têm responsável, ")
                .append("escolhe o barista menos ocupado e o equipamento adequado ao produto, ")
                .append("e move os pedidos para \"Em preparo\".</p>");
        sb.append("<form class=\"form-linha\" method=\"post\" action=\"/procedimentos/distribuir\">");
        sb.append("<button type=\"submit\">Distribuir itens pendentes</button></form>");
        sb.append("</section>");

        sb.append("<section class=\"secao\"><h2>Pedidos</h2>");
        sb.append("<div class=\"tabela-caixa\"><table><tr><th>Pedido</th><th>Data</th><th>Status</th>")
                .append("<th>Itens</th><th>Itens com responsável</th><th>Total</th></tr>");
        for (PedidoStatusDTO p : pedidos) {
            sb.append("<tr><td>#").append(p.idPagamento()).append("</td>")
                    .append("<td>").append(p.data().format(DATA)).append("</td>")
                    .append("<td>").append(escapar(p.status())).append("</td>")
                    .append("<td>").append(p.itens()).append("</td>")
                    .append("<td>").append(p.itensAtribuidos()).append("</td>")
                    .append("<td>").append(moeda(p.valorTotal())).append("</td></tr>");
        }
        sb.append("</table></div></section>");

        sb.append("<section class=\"secao\"><h2>Quem está preparando o quê</h2>");
        sb.append("<p class=\"descricao\">Itens dos pedidos \"Em preparo\", com o barista e o equipamento ")
                .append("definidos pela distribuição.</p>");
        sb.append("<div class=\"tabela-caixa\"><table><tr><th>Pedido</th><th>Item</th><th>Produto</th>")
                .append("<th>Barista</th><th>Equipamento</th></tr>");
        if (preparos.isEmpty()) {
            sb.append("<tr><td colspan=\"5\">Nenhum item em preparo no momento.</td></tr>");
        }
        for (PreparoDTO r : preparos) {
            sb.append("<tr><td>#").append(r.idPagamento()).append("</td>")
                    .append("<td>").append(r.numeroItem()).append("</td>")
                    .append("<td>").append(escapar(r.produto())).append("</td>")
                    .append("<td>").append(escapar(r.barista())).append("</td>")
                    .append("<td>").append(escapar(r.equipamento())).append("</td></tr>");
        }
        sb.append("</table></div></section>");
        return sb.toString();
    }
}
