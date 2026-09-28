package http;

import model.Cliente;

import java.util.List;

import static http.Html.escapar;

public class ClienteView {

    /** Lista de clientes, com mensagem de erro opcional (null se não houver). */
    public static String tabela(List<Cliente> clientes, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<p style=\"color:#b00020\"><strong>").append(escapar(mensagemErro)).append("</strong></p>");
        }

        sb.append("<p><a href=\"/clientes/novo\">+ Novo cliente</a></p>");
        sb.append("<table><tr><th>Nome</th><th>E-mail</th><th>Cadastro</th><th>Ações</th></tr>");

        for (Cliente c : clientes) {
            sb.append("<tr>");
            sb.append("<td>").append(escapar(c.getNome())).append("</td>");
            sb.append("<td>").append(escapar(c.getEmail())).append("</td>");
            sb.append("<td>").append(c.getDataCadastro()).append("</td>");
            sb.append("<td>");
            sb.append("<a href=\"/clientes/editar?id=").append(c.getIDCliente()).append("\">Editar</a> ");
            sb.append("<form method=\"post\" action=\"/clientes/excluir\" style=\"display:inline\" ")
                    .append("onsubmit=\"return confirm('Excluir este cliente?')\">");
            sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(c.getIDCliente()).append("\">");
            sb.append("<button type=\"submit\">Excluir</button>");
            sb.append("</form>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }

    /** Formulário de novo cliente (ID = 0) ou de edição (ID != 0). */
    public static String formulario(Cliente c, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<p style=\"color:#b00020\"><strong>").append(escapar(mensagemErro)).append("</strong></p>");
        }

        sb.append("<form method=\"post\" action=\"/clientes/salvar\">");
        sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(c.getIDCliente()).append("\">");
        sb.append("<p><label>Nome<br><input type=\"text\" name=\"nome\" maxlength=\"40\" required value=\"")
                .append(escapar(c.getNome())).append("\"></label></p>");
        sb.append("<p><label>E-mail<br><input type=\"email\" name=\"email\" maxlength=\"40\" required value=\"")
                .append(escapar(c.getEmail())).append("\"></label></p>");
        sb.append("<button type=\"submit\">Salvar</button> ");
        sb.append("<a href=\"/clientes\">Cancelar</a>");
        sb.append("</form>");

        return sb.toString();
    }
}