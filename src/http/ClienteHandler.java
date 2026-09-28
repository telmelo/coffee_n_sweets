package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ClienteDAO;
import model.Cliente;
import util.Resposta;
import view.Layout;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Rotas de clientes:
 *   GET  /clientes          -> lista
 *   GET  /clientes/novo     -> formulário vazio
 *   GET  /clientes/editar   -> formulário preenchido (?id=)
 *   POST /clientes/salvar   -> insere (id = 0) ou atualiza (id != 0)
 *   POST /clientes/excluir  -> exclui
 */
public class ClienteHandler implements HttpHandler {
    private final ClienteDAO clienteDAO = new ClienteDAO();

    private static final Pattern PADRAO_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$");

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        try {
            if (metodo.equals("GET")) {
                switch (caminho) {
                    case "/clientes" -> mostrarLista(troca, null);
                    case "/clientes/novo" -> mostrarFormulario(troca, new Cliente(), null);
                    case "/clientes/editar" -> editar(troca);
                    default -> Resposta.naoEncontrado(troca);
                }
            } else if (metodo.equals("POST")) {
                switch (caminho) {
                    case "/clientes/salvar" -> salvar(troca);
                    case "/clientes/excluir" -> excluir(troca);
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
        List<Cliente> clientes = clienteDAO.listar();
        Resposta.html(troca, Layout.pagina("Clientes", ClienteView.tabela(clientes, mensagemErro)));
    }

    private void mostrarFormulario(HttpExchange troca, Cliente cliente, String mensagemErro) throws IOException {
        String titulo = cliente.getIDCliente() == 0 ? "Novo cliente" : "Editar cliente";
        Resposta.html(troca, Layout.pagina(titulo, ClienteView.formulario(cliente, mensagemErro)));
    }

    private void editar(HttpExchange troca) throws IOException, SQLException {
        int id = Formulario.inteiro(Formulario.lerQuery(troca).get("id"));
        Cliente cliente = clienteDAO.buscarPorId(id);
        if (cliente == null) {
            Resposta.naoEncontrado(troca);
            return;
        }
        mostrarFormulario(troca, cliente, null);
    }

    private void salvar(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);

        Cliente cliente = new Cliente();
        cliente.setIDCliente(Formulario.inteiro(form.get("id")));
        cliente.setNome(form.getOrDefault("nome", "").trim());
        cliente.setEmail(form.getOrDefault("email", "").trim());

        String erro = validar(cliente);
        if (erro != null) {
            mostrarFormulario(troca, cliente, erro);
            return;
        }

        try {
            if (cliente.getIDCliente() == 0) {
                cliente.setDataCadastro(LocalDate.now());
                clienteDAO.inserir(cliente);
            } else {
                Cliente existente = clienteDAO.buscarPorId(cliente.getIDCliente());
                if (existente == null) {
                    Resposta.naoEncontrado(troca);
                    return;
                }
                cliente.setDataCadastro(existente.getDataCadastro()); // mantém a data original
                clienteDAO.atualizar(cliente);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // constraint unique_email_cliente
            mostrarFormulario(troca, cliente, "Já existe um cliente com esse e-mail.");
            return;
        }

        Formulario.redirecionar(troca, "/clientes");
    }

    private void excluir(HttpExchange troca) throws IOException, SQLException {
        int id = Formulario.inteiro(Formulario.lerCorpo(troca).get("id"));
        try {
            clienteDAO.excluir(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            // FK de Pedido_pagamento / Telefone_cliente impede a exclusão
            mostrarLista(troca, "Não foi possível excluir: este cliente possui pedidos ou telefones vinculados.");
            return;
        }
        Formulario.redirecionar(troca, "/clientes");
    }

    /** Devolve a mensagem de erro, ou null se o cliente for válido. */
    private String validar(Cliente c) {
        if (c.getNome().isBlank()) return "O nome não pode ser vazio.";
        if (c.getNome().length() > 40) return "O nome não pode ter mais de 40 caracteres.";
        if (c.getEmail().length() > 40) return "O e-mail não pode ter mais de 40 caracteres.";
        if (!PADRAO_EMAIL.matcher(c.getEmail()).matches()) return "E-mail inválido.";
        return null;
    }
}