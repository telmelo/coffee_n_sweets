package http;

import model.Produto;

import java.util.List;
import java.util.Locale;

import static http.Html.escapar;

public class ProdutoView {

    /** Lista de produtos, com mensagem de erro opcional (null se não houver). */
    public static String tabela(List<Produto> produtos, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<p style=\"color:#b00020\"><strong>").append(escapar(mensagemErro)).append("</strong></p>");
        }

        sb.append("<p><a href=\"/produtos/novo\">+ Novo produto</a></p>");
        sb.append("<table><tr><th>Nome</th><th>Preço</th><th>Descrição</th><th>Ações</th></tr>");

        for (Produto p : produtos) {
            sb.append("<tr>");
            sb.append("<td>").append(escapar(p.getNome())).append("</td>");
            sb.append("<td>").append(formatarPreco(p.getPreco())).append("</td>");
            sb.append("<td>").append(escapar(p.getDescricao())).append("</td>");
            sb.append("<td>");
            sb.append("<a href=\"/produtos/editar?id=").append(p.getIDProduto()).append("\">Editar</a> ");
            sb.append("<form method=\"post\" action=\"/produtos/excluir\" style=\"display:inline\" ")
                    .append("onsubmit=\"return confirm('Excluir este produto?')\">");
            sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(p.getIDProduto()).append("\">");
            sb.append("<button type=\"submit\">Excluir</button>");
            sb.append("</form>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }

    /**
     * Formulário de novo produto (ID = 0) ou de edição (ID != 0).
     * textoPreco é o que o usuário digitou (para reexibir em caso de erro).
     */
    public static String formulario(Produto p, String textoPreco, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<p style=\"color:#b00020\"><strong>").append(escapar(mensagemErro)).append("</strong></p>");
        }

        sb.append("<form method=\"post\" action=\"/produtos/salvar\">");
        sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(p.getIDProduto()).append("\">");
        sb.append("<p><label>Nome<br><input type=\"text\" name=\"nome\" maxlength=\"40\" required value=\"")
                .append(escapar(p.getNome())).append("\"></label></p>");
        sb.append("<p><label>Preço (R$)<br><input type=\"text\" name=\"preco\" required value=\"")
                .append(escapar(textoPreco)).append("\"></label></p>");
        sb.append("<p><label>Descrição<br><textarea name=\"descricao\" rows=\"4\" cols=\"40\" required>")
                .append(escapar(p.getDescricao())).append("</textarea></label></p>");
        sb.append("<button type=\"submit\">Salvar</button> ");
        sb.append("<a href=\"/produtos\">Cancelar</a>");
        sb.append("</form>");

        return sb.toString();
    }

    private static String formatarPreco(float preco) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", preco);
    }
}