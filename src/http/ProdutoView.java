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
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        sb.append("<div class=\"barra\"><a class=\"botao\" href=\"/produtos/novo\">+ Novo produto</a></div>");
        sb.append("<div class=\"tabela-caixa\"><table><tr><th>Nome</th><th>Preço</th><th>Descrição</th><th>Ações</th></tr>");

        for (Produto p : produtos) {
            sb.append("<tr>");
            sb.append("<td>").append(escapar(p.getNome())).append("</td>");
            sb.append("<td>").append(formatarPreco(p.getPreco())).append("</td>");
            sb.append("<td>").append(escapar(p.getDescricao())).append("</td>");
            sb.append("<td><div class=\"acoes\">");
            sb.append("<a class=\"botao botao-pequeno botao-secundario\" href=\"/produtos/editar?id=").append(p.getIDProduto()).append("\">Editar</a>");
            sb.append("<form method=\"post\" action=\"/produtos/excluir\" class=\"form-inline\" ")
                    .append("onsubmit=\"return confirm('Excluir este produto?')\">");
            sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(p.getIDProduto()).append("\">");
            sb.append("<button type=\"submit\" class=\"botao-pequeno botao-perigo\">Excluir</button>");
            sb.append("</form>");
            sb.append("</div></td>");
            sb.append("</tr>");
        }
        sb.append("</table></div>");

        return sb.toString();
    }

    /**
     * Formulário de novo produto (ID = 0) ou de edição (ID != 0).
     * textoPreco é o que o usuário digitou (para reexibir em caso de erro).
     */
    public static String formulario(Produto p, String textoPreco, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        sb.append("<form class=\"formulario\" method=\"post\" action=\"/produtos/salvar\">");
        sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(p.getIDProduto()).append("\">");
        sb.append("<div class=\"campo\"><label for=\"nome\">Nome</label><input id=\"nome\" type=\"text\" name=\"nome\" maxlength=\"40\" required value=\"")
                .append(escapar(p.getNome())).append("\"></div>");
        sb.append("<div class=\"campo\"><label for=\"preco\">Preço (R$)</label><input id=\"preco\" type=\"text\" name=\"preco\" required value=\"")
                .append(escapar(textoPreco)).append("\"></div>");
        sb.append("<div class=\"campo\"><label for=\"descricao\">Descrição</label><textarea id=\"descricao\" name=\"descricao\" rows=\"4\" cols=\"40\" required>")
                .append(escapar(p.getDescricao())).append("</textarea></div>");
        sb.append("<div class=\"formulario-acoes\"><button type=\"submit\">Salvar</button>");
        sb.append("<a class=\"botao botao-secundario\" href=\"/produtos\">Cancelar</a></div>");
        sb.append("</form>");

        return sb.toString();
    }

    private static String formatarPreco(float preco) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", preco);
    }
}