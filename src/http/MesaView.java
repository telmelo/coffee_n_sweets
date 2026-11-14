package http;

import model.Mesa;

import java.util.List;

import static http.Html.escapar;

public class MesaView {

    public static String tabela(List<Mesa> mesas, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        sb.append("<div class=\"barra\"><a class=\"botao\" href=\"/mesas/novo\">+ Nova mesa</a></div>");
        sb.append("<div class=\"tabela-caixa\"><table><tr><th>Número</th><th>Capacidade</th><th>Localização</th><th>Ações</th></tr>");

        for (Mesa m : mesas) {
            sb.append("<tr>");
            sb.append("<td>").append(m.getNumero()).append("</td>");
            sb.append("<td>").append(m.getCapacidade()).append(" lugares</td>");
            sb.append("<td>").append(escapar(m.getLocalizacao())).append("</td>");
            sb.append("<td><div class=\"acoes\">");
            sb.append("<a class=\"botao botao-pequeno botao-secundario\" href=\"/mesas/editar?numero=").append(m.getNumero()).append("\">Editar</a>");
            sb.append("<form method=\"post\" action=\"/mesas/excluir\" class=\"form-inline\" ")
                    .append("onsubmit=\"return confirm('Excluir esta mesa? Os pedidos dela ficarão sem mesa.')\">");
            sb.append("<input type=\"hidden\" name=\"numero\" value=\"").append(m.getNumero()).append("\">");
            sb.append("<button type=\"submit\" class=\"botao-pequeno botao-perigo\">Excluir</button>");
            sb.append("</form>");
            sb.append("</div></td>");
            sb.append("</tr>");
        }
        sb.append("</table></div>");

        return sb.toString();
    }

    public static String formulario(Mesa m, boolean novo, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        String numero = m.getNumero() == 0 ? "" : String.valueOf(m.getNumero());
        String capacidade = m.getCapacidade() == 0 ? "" : String.valueOf(m.getCapacidade());

        sb.append("<form class=\"formulario\" method=\"post\" action=\"/mesas/salvar\">");
        sb.append("<input type=\"hidden\" name=\"novo\" value=\"").append(novo ? "1" : "0").append("\">");
        sb.append("<div class=\"campo\"><label for=\"numero\">Número da mesa</label>")
                .append("<input id=\"numero\" type=\"number\" name=\"numero\" min=\"1\" required value=\"").append(numero).append("\"")
                .append(novo ? "" : " readonly").append("></div>");
        sb.append("<div class=\"campo\"><label for=\"capacidade\">Capacidade</label>")
                .append("<input id=\"capacidade\" type=\"number\" name=\"capacidade\" min=\"2\" required value=\"").append(capacidade).append("\"></div>");

        sb.append("<div class=\"campo\"><label for=\"localizacao\">Localização</label><select id=\"localizacao\" name=\"localizacao\">");
        for (String loc : Mesa.LOCALIZACOES) {
            sb.append("<option value=\"").append(escapar(loc)).append("\"")
                    .append(loc.equals(m.getLocalizacao()) ? " selected" : "")
                    .append(">").append(escapar(loc)).append("</option>");
        }
        sb.append("</select></div>");

        sb.append("<div class=\"formulario-acoes\"><button type=\"submit\">Salvar</button>");
        sb.append("<a class=\"botao botao-secundario\" href=\"/mesas\">Cancelar</a></div>");
        sb.append("</form>");

        return sb.toString();
    }
}