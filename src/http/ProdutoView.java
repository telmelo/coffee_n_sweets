package http;

import model.Produto;

import java.util.List;

import static http.Html.escapar;

public class ProdutoView {
    public static String tabela(List<Produto> produtos){
        StringBuilder sb = new StringBuilder();
        sb.append("<table><tr><th>Nome</th><th>Preço</th><th>Ações</th></tr>");

        for (Produto p : produtos) {
            sb.append("<tr>");
            sb.append("<td>").append(escapar(p.getNome())).append("</td>");
            sb.append("<td>").append(escapar(String.valueOf(p.getPreco()))).append("</td>");
            sb.append("<td>");
            sb.append("<a href=\"/produtos/editar?id=").append(p.getIDProduto()).append("\">Editar</a> ");
            sb.append("<form method=\"post\" action=\"/produtos/excluir\">");
            sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(p.getIDProduto()).append("\">");
            sb.append("<button type=\"submit\">Excluir</button>");
            sb.append("</form>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }
}