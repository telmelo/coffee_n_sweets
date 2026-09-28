package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ProdutoDAO;
import model.Produto;
import util.Resposta;
import view.Layout;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

/**
 * Rotas de produtos:
 *   GET  /produtos          -> lista
 *   GET  /produtos/novo     -> formulário vazio
 *   GET  /produtos/editar   -> formulário preenchido (?id=)
 *   POST /produtos/salvar   -> insere (id = 0) ou atualiza (id != 0)
 *   POST /produtos/excluir  -> exclui
 */
public class ProdutoHandler implements HttpHandler {
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        try {
            if (metodo.equals("GET")) {
                switch (caminho) {
                    case "/produtos" -> mostrarLista(troca, null);
                    case "/produtos/novo" -> mostrarFormulario(troca, new Produto(), "", null);
                    case "/produtos/editar" -> editar(troca);
                    default -> Resposta.naoEncontrado(troca);
                }
            } else if (metodo.equals("POST")) {
                switch (caminho) {
                    case "/produtos/salvar" -> salvar(troca);
                    case "/produtos/excluir" -> excluir(troca);
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
        List<Produto> produtos = produtoDAO.listar();
        Resposta.html(troca, Layout.pagina("Produtos", ProdutoView.tabela(produtos, mensagemErro)));
    }

    private void mostrarFormulario(HttpExchange troca, Produto produto, String textoPreco, String mensagemErro)
            throws IOException {
        String titulo = produto.getIDProduto() == 0 ? "Novo produto" : "Editar produto";
        Resposta.html(troca, Layout.pagina(titulo, ProdutoView.formulario(produto, textoPreco, mensagemErro)));
    }

    private void editar(HttpExchange troca) throws IOException, SQLException {
        int id = Formulario.inteiro(Formulario.lerQuery(troca).get("id"));
        Produto produto = produtoDAO.buscarPorId(id);
        if (produto == null) {
            Resposta.naoEncontrado(troca);
            return;
        }
        mostrarFormulario(troca, produto, String.valueOf(produto.getPreco()), null);
    }

    private void salvar(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);

        Produto produto = new Produto();
        produto.setIDProduto(Formulario.inteiro(form.get("id")));
        produto.setNome(form.getOrDefault("nome", "").trim());
        produto.setDescricao(form.getOrDefault("descricao", "").trim());

        String textoPreco = form.getOrDefault("preco", "").trim();
        String erro = null;
        try {
            // aceita vírgula ou ponto como separador decimal
            produto.setPreco(Float.parseFloat(textoPreco.replace(',', '.')));
        } catch (NumberFormatException e) {
            erro = "Preço inválido. Use apenas números (ex: 12,50).";
        }

        if (erro == null) {
            erro = validar(produto);
        }
        if (erro != null) {
            mostrarFormulario(troca, produto, textoPreco, erro);
            return;
        }

        try {
            if (produto.getIDProduto() == 0) {
                produtoDAO.inserir(produto);
            } else {
                if (produtoDAO.buscarPorId(produto.getIDProduto()) == null) {
                    Resposta.naoEncontrado(troca);
                    return;
                }
                produtoDAO.atualizar(produto);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // constraint unique_produto (nome único)
            mostrarFormulario(troca, produto, textoPreco, "Já existe um produto com esse nome.");
            return;
        }

        Formulario.redirecionar(troca, "/produtos");
    }

    private void excluir(HttpExchange troca) throws IOException, SQLException {
        int id = Formulario.inteiro(Formulario.lerCorpo(troca).get("id"));
        try {
            produtoDAO.excluir(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            // FK de Item_pedido impede a exclusão
            mostrarLista(troca, "Não foi possível excluir: este produto já foi vendido (existe em pedidos).");
            return;
        }
        Formulario.redirecionar(troca, "/produtos");
    }

    /** Devolve a mensagem de erro, ou null se o produto for válido. */
    private String validar(Produto p) {
        if (p.getNome().isBlank()) return "O nome não pode ser vazio.";
        if (p.getNome().length() > 40) return "O nome não pode ter mais de 40 caracteres.";
        if (p.getPreco() <= 0) return "O preço deve ser maior que zero.";
        if (p.getDescricao().isBlank()) return "A descrição não pode ser vazia.";
        return null;
    }
}