package http;

import model.Funcionario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static http.Html.escapar;

public class FuncionarioView {

    public static String tabela(List<Funcionario> funcionarios, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        Map<Integer, String> nomes = new HashMap<>();
        for (Funcionario f : funcionarios) {
            nomes.put(f.getIDFuncionario(), f.getNome());
        }

        if (mensagemErro != null) {
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        sb.append("<div class=\"barra\"><a class=\"botao\" href=\"/funcionarios/novo\">+ Novo funcionário</a></div>");
        sb.append("<div class=\"tabela-caixa\"><table><tr><th>Nome</th><th>Cargo</th><th>Admissão</th><th>Supervisor</th><th>Ações</th></tr>");

        for (Funcionario f : funcionarios) {
            String supervisor = f.getIDSupervisor() == null ? "—" : nomes.getOrDefault(f.getIDSupervisor(), "—");

            sb.append("<tr>");
            sb.append("<td>").append(escapar(f.getNome())).append("</td>");
            sb.append("<td>").append(escapar(f.getCargo())).append("</td>");
            sb.append("<td>").append(f.getDataAdmissao()).append("</td>");
            sb.append("<td>").append(escapar(supervisor)).append("</td>");
            sb.append("<td><div class=\"acoes\">");
            sb.append("<a class=\"botao botao-pequeno botao-secundario\" href=\"/funcionarios/editar?id=").append(f.getIDFuncionario()).append("\">Editar</a>");
            sb.append("<form method=\"post\" action=\"/funcionarios/excluir\" class=\"form-inline\" ")
                    .append("onsubmit=\"return confirm('Excluir este funcionário?')\">");
            sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(f.getIDFuncionario()).append("\">");
            sb.append("<button type=\"submit\" class=\"botao-pequeno botao-perigo\">Excluir</button>");
            sb.append("</form>");
            sb.append("</div></td>");
            sb.append("</tr>");
        }
        sb.append("</table></div>");

        return sb.toString();
    }

    public static String formulario(Funcionario f, List<Funcionario> possiveisSupervisores, String mensagemErro) {
        StringBuilder sb = new StringBuilder();

        if (mensagemErro != null) {
            sb.append("<div class=\"alerta\">").append(escapar(mensagemErro)).append("</div>");
        }

        String data = f.getDataAdmissao() == null ? "" : f.getDataAdmissao().toString();

        sb.append("<form class=\"formulario\" method=\"post\" action=\"/funcionarios/salvar\">");
        sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(f.getIDFuncionario()).append("\">");
        sb.append("<div class=\"campo\"><label for=\"nome\">Nome</label><input id=\"nome\" type=\"text\" name=\"nome\" maxlength=\"40\" required value=\"")
                .append(escapar(f.getNome())).append("\"></div>");

        sb.append("<div class=\"campo\"><label for=\"cargo\">Cargo</label><select id=\"cargo\" name=\"cargo\">");
        for (String cargo : Funcionario.CARGOS) {
            sb.append("<option value=\"").append(escapar(cargo)).append("\"")
                    .append(cargo.equals(f.getCargo()) ? " selected" : "")
                    .append(">").append(escapar(cargo)).append("</option>");
        }
        sb.append("</select></div>");

        sb.append("<div class=\"campo\"><label for=\"dataAdmissao\">Data de admissão</label>")
                .append("<input id=\"dataAdmissao\" type=\"date\" name=\"dataAdmissao\" required value=\"").append(data).append("\"></div>");

        sb.append("<div class=\"campo\"><label for=\"supervisor\">Supervisor</label><select id=\"supervisor\" name=\"supervisor\">");
        sb.append("<option value=\"\">Sem supervisor</option>");
        for (Funcionario s : possiveisSupervisores) {
            if (s.getIDFuncionario() == f.getIDFuncionario()) continue;
            boolean selecionado = f.getIDSupervisor() != null && f.getIDSupervisor() == s.getIDFuncionario();
            sb.append("<option value=\"").append(s.getIDFuncionario()).append("\"")
                    .append(selecionado ? " selected" : "")
                    .append(">").append(escapar(s.getNome())).append(" (").append(escapar(s.getCargo())).append(")</option>");
        }
        sb.append("</select></div>");

        sb.append("<div class=\"formulario-acoes\"><button type=\"submit\">Salvar</button>");
        sb.append("<a class=\"botao botao-secundario\" href=\"/funcionarios\">Cancelar</a></div>");
        sb.append("</form>");

        return sb.toString();
    }
}