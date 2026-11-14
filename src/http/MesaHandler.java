package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.MesaDAO;
import model.Mesa;
import util.Resposta;
import view.Layout;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

public class MesaHandler implements HttpHandler {
    private final MesaDAO mesaDAO = new MesaDAO();

    // Código de erro do MySQL 8 quando um CHECK é violado
    private static final int ERRO_CHECK = 3819;

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        try {
            if (metodo.equals("GET")) {
                switch (caminho) {
                    case "/mesas" -> mostrarLista(troca, null);
                    case "/mesas/novo" -> mostrarFormulario(troca, new Mesa(), true, null);
                    case "/mesas/editar" -> editar(troca);
                    default -> Resposta.naoEncontrado(troca);
                }
            } else if (metodo.equals("POST")) {
                switch (caminho) {
                    case "/mesas/salvar" -> salvar(troca);
                    case "/mesas/excluir" -> excluir(troca);
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
        List<Mesa> mesas = mesaDAO.listar();
        Resposta.html(troca, Layout.pagina("Mesas", MesaView.tabela(mesas, mensagemErro)));
    }

    private void mostrarFormulario(HttpExchange troca, Mesa mesa, boolean novo, String mensagemErro) throws IOException {
        String titulo = novo ? "Nova mesa" : "Editar mesa";
        Resposta.html(troca, Layout.pagina(titulo, MesaView.formulario(mesa, novo, mensagemErro)));
    }

    private void editar(HttpExchange troca) throws IOException, SQLException {
        int numero = Formulario.inteiro(Formulario.lerQuery(troca).get("numero"));
        Mesa mesa = mesaDAO.buscarPorNumero(numero);
        if (mesa == null) {
            Resposta.naoEncontrado(troca);
            return;
        }
        mostrarFormulario(troca, mesa, false, null);
    }

    private void salvar(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);
        boolean novo = "1".equals(form.get("novo"));

        Mesa mesa = new Mesa();
        mesa.setNumero(Formulario.inteiro(form.get("numero")));
        mesa.setCapacidade(Formulario.inteiro(form.get("capacidade")));
        mesa.setLocalizacao(form.getOrDefault("localizacao", "").trim());

        String erro = validar(mesa);
        if (erro != null) {
            mostrarFormulario(troca, mesa, novo, erro);
            return;
        }

        try {
            if (novo) {
                mesaDAO.inserir(mesa);
            } else {
                if (mesaDAO.buscarPorNumero(mesa.getNumero()) == null) {
                    Resposta.naoEncontrado(troca);
                    return;
                }
                mesaDAO.atualizar(mesa);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // chave primária duplicada
            mostrarFormulario(troca, mesa, novo, "Já existe uma mesa com esse número.");
            return;
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_CHECK) {
                mostrarFormulario(troca, mesa, novo,
                        "O banco recusou os dados: a capacidade deve ser de no mínimo 2 e a localização deve ser válida.");
                return;
            }
            throw e;
        }

        Formulario.redirecionar(troca, "/mesas");
    }

    private void excluir(HttpExchange troca) throws IOException, SQLException {
        int numero = Formulario.inteiro(Formulario.lerCorpo(troca).get("numero"));
        mesaDAO.excluir(numero);
        Formulario.redirecionar(troca, "/mesas");
    }

    private String validar(Mesa m) {
        if (m.getNumero() <= 0) return "O número da mesa deve ser maior que zero.";
        if (m.getCapacidade() < 2) return "A capacidade deve ser de no mínimo 2 lugares.";
        if (!Mesa.LOCALIZACOES.contains(m.getLocalizacao())) return "Localização inválida.";
        return null;
    }
}