package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.FuncionarioDAO;
import model.Funcionario;
import util.Resposta;
import view.Layout;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class FuncionarioHandler implements HttpHandler {
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    private static final int ERRO_CHECK = 3819;

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        try {
            if (metodo.equals("GET")) {
                switch (caminho) {
                    case "/funcionarios" -> mostrarLista(troca, null);
                    case "/funcionarios/novo" -> {
                        Funcionario novo = new Funcionario();
                        novo.setDataAdmissao(LocalDate.now());
                        mostrarFormulario(troca, novo, null);
                    }
                    case "/funcionarios/editar" -> editar(troca);
                    default -> Resposta.naoEncontrado(troca);
                }
            } else if (metodo.equals("POST")) {
                switch (caminho) {
                    case "/funcionarios/salvar" -> salvar(troca);
                    case "/funcionarios/excluir" -> excluir(troca);
                    default -> Resposta.naoEncontrado(troca);
                }
            } else {
                Resposta.naoEncontrado(troca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Resposta.erro(troca, e.getMessage());
        }
    }

    private void mostrarLista(HttpExchange troca, String mensagemErro) throws IOException, SQLException {
        List<Funcionario> funcionarios = funcionarioDAO.listar();
        Resposta.html(troca, Layout.pagina("Funcionários", FuncionarioView.tabela(funcionarios, mensagemErro)));
    }

    private void mostrarFormulario(HttpExchange troca, Funcionario f, String mensagemErro) throws IOException, SQLException {
        String titulo = f.getIDFuncionario() == 0 ? "Novo funcionário" : "Editar funcionário";
        List<Funcionario> possiveisSupervisores = funcionarioDAO.listar();
        Resposta.html(troca, Layout.pagina(titulo, FuncionarioView.formulario(f, possiveisSupervisores, mensagemErro)));
    }

    private void editar(HttpExchange troca) throws IOException, SQLException {
        int id = Formulario.inteiro(Formulario.lerQuery(troca).get("id"));
        Funcionario f = funcionarioDAO.buscarPorId(id);
        if (f == null) {
            Resposta.naoEncontrado(troca);
            return;
        }
        mostrarFormulario(troca, f, null);
    }

    private void salvar(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);

        Funcionario f = new Funcionario();
        f.setIDFuncionario(Formulario.inteiro(form.get("id")));
        f.setNome(form.getOrDefault("nome", "").trim());
        f.setCargo(form.getOrDefault("cargo", "").trim());

        try {
            f.setDataAdmissao(LocalDate.parse(form.getOrDefault("dataAdmissao", "").trim()));
        } catch (DateTimeParseException e) {
            f.setDataAdmissao(null);
        }

        String supervisor = form.getOrDefault("supervisor", "").trim();
        f.setIDSupervisor(supervisor.isEmpty() ? null : Formulario.inteiro(supervisor));

        String erro = validar(f);
        if (erro != null) {
            mostrarFormulario(troca, f, erro);
            return;
        }

        try {
            if (f.getIDFuncionario() == 0) {
                funcionarioDAO.inserir(f);
            } else {
                if (funcionarioDAO.buscarPorId(f.getIDFuncionario()) == null) {
                    Resposta.naoEncontrado(troca);
                    return;
                }
                funcionarioDAO.atualizar(f);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            mostrarFormulario(troca, f, "O supervisor escolhido não existe.");
            return;
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_CHECK) {
                mostrarFormulario(troca, f, "O banco recusou os dados: o cargo deve ser Atendente, Barista ou Gerente.");
                return;
            }
            throw e;
        }

        Formulario.redirecionar(troca, "/funcionarios");
    }

    private void excluir(HttpExchange troca) throws IOException, SQLException {
        int id = Formulario.inteiro(Formulario.lerCorpo(troca).get("id"));
        try {
            funcionarioDAO.excluir(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            mostrarLista(troca, "Não foi possível excluir: este funcionário possui pedidos, preparos ou telefones vinculados.");
            return;
        }
        Formulario.redirecionar(troca, "/funcionarios");
    }

    private String validar(Funcionario f) {
        if (f.getNome().isBlank()) return "O nome não pode ser vazio.";
        if (f.getNome().length() > 40) return "O nome não pode ter mais de 40 caracteres.";
        if (!Funcionario.CARGOS.contains(f.getCargo())) return "Cargo inválido.";
        if (f.getDataAdmissao() == null) return "Data de admissão inválida.";
        if (f.getDataAdmissao().isAfter(LocalDate.now())) return "A data de admissão não pode ser no futuro.";
        if (f.getIDSupervisor() != null && f.getIDSupervisor() == f.getIDFuncionario()) {
            return "Um funcionário não pode ser supervisor de si mesmo.";
        }
        return null;
    }
}