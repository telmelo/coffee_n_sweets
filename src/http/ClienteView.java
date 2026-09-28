package http;

import model.Cliente;

import java.util.List;

import static http.Html.escapar;

public class ClienteView {

    /** Lista de clientes, com mensagem de erro opcional (null se não houver). */
    public static String tabela(List<Cliente> clientes, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        sb.append("<div class=\"barra\"><a class=\"botao\" href=\"/clientes/novo\">+ Novo cliente</a></div>");
        sb.append("<div class=\"tabela-caixa\"><table><tr><th>Nome</th><th>E-mail</th><th>Cadastro</th><th>Ações</th></tr>");

        for (Cliente c : clientes) {
            sb.append("<tr>");
            sb.append("<td>").append(escapar(c.getNome())).append("</td>");
            sb.append("<td>").append(escapar(c.getEmail())).append("</td>");
            sb.append("<td>").append(c.getDataCadastro()).append("</td>");
            sb.append("<td><div class=\"acoes\">");
            sb.append("<a class=\"botao botao-pequeno botao-secundario\" href=\"/clientes/editar?id=").append(c.getIDCliente()).append("\">Editar</a>");
            sb.append("<form method=\"post\" action=\"/clientes/excluir\" class=\"form-inline\" ")
                    .append("onsubmit=\"return confirm('Excluir este cliente?')\">");
            sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(c.getIDCliente()).append("\">");
            sb.append("<button type=\"submit\" class=\"botao-pequeno botao-perigo\">Excluir</button>");
            sb.append("</form>");
            sb.append("</div></td>");
            sb.append("</tr>");
        }
        sb.append("</table></div>");

        return sb.toString();
    }

    /** Formulário de novo cliente (ID = 0) ou de edição (ID != 0). */
    public static String formulario(Cliente c, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        sb.append("<form class=\"formulario\" method=\"post\" action=\"/clientes/salvar\">");
        sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(c.getIDCliente()).append("\">");
        sb.append("<div class=\"campo\"><label for=\"nome\">Nome</label><input id=\"nome\" type=\"text\" name=\"nome\" maxlength=\"40\" required value=\"")
                .append(escapar(c.getNome())).append("\"></div>");
        sb.append("<div class=\"campo\"><label for=\"email\">E-mail</label><input id=\"email\" type=\"email\" name=\"email\" maxlength=\"40\" required value=\"")
                .append(escapar(c.getEmail())).append("\"></div>");
        sb.append("<div class=\"formulario-acoes\"><button type=\"submit\">Salvar</button>");
        sb.append("<a class=\"botao botao-secundario\" href=\"/clientes\">Cancelar</a></div>");
        sb.append("</form>");

        return sb.toString();
    }
}