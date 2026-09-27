package http;

import model.Cliente;

import java.util.List;

import static http.Html.escapar;

public class ClienteView {
    public static String tabela(List<Cliente> clientes){
        StringBuilder sb = new StringBuilder();
        sb.append("<table><tr><th>Nome</th><th>E-mail</th><th>Ações</th></tr>");

        for (Cliente c : clientes) {
            sb.append("<tr>");
            sb.append("<td>").append(escapar(c.getNome())).append("</td>");
            sb.append("<td>").append(escapar(c.getEmail())).append("</td>");
            sb.append("<td>");
            sb.append("<a href=\"/clientes/editar?id=").append(c.getIDCliente()).append("\">Editar</a> ");
            sb.append("<form method=\"post\" action=\"/clientes/excluir\">");
            sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(c.getIDCliente()).append("\">");
            sb.append("<button type=\"submit\">Excluir</button>");
            sb.append("</form>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }
}
