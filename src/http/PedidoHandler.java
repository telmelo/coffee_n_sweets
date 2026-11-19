package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ClienteDAO;
import dao.FuncionarioDAO;
import dao.ItemPedidoDAO;
import dao.MesaDAO;
import dao.PedidoDAO;
import dto.PedidoResumoDTO;
import model.Cliente;
import model.Pedido;
import util.Resposta;
import view.Layout;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidoHandler implements HttpHandler {
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ItemPedidoDAO itemDAO = new ItemPedidoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
    private final MesaDAO mesaDAO = new MesaDAO();

    // código de erro do MySQL 8 quando um CHECK é violado
    private static final int ERRO_CHECK = 3819;

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        try {
            if (metodo.equals("GET")) {
                switch (caminho) {
                    case "/pedidos" -> mostrarLista(troca);
                    case "/pedidos/novo" -> mostrarFormulario(troca, new Pedido(), null);
                    case "/pedidos/editar" -> editar(troca);
                    case "/pedidos/ver" -> ver(troca);
                    default -> Resposta.naoEncontrado(troca);
                }
            } else if (metodo.equals("POST")) {
                switch (caminho) {
                    case "/pedidos/salvar" -> salvar(troca);
                    case "/pedidos/excluir" -> excluir(troca);
                    case "/pedidos/status" -> avancarStatus(troca);
                    case "/pedidos/itens/adicionar" -> adicionarItem(troca);
                    case "/pedidos/itens/remover" -> removerItem(troca);
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

    // ---------- telas ----------

    private void mostrarLista(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> q = consulta(troca);
        String status = q.get("status");
        if (status == null || !Pedido.STATUS.contains(status)) status = null;

        List<PedidoResumoDTO> pedidos = pedidoDAO.listarResumos(status);
        Resposta.html(troca, Layout.pagina("Pedidos",
                PedidoView.lista(pedidos, status, q.get("ok"), q.get("erro"))));
    }

    private void mostrarFormulario(HttpExchange troca, Pedido pedido, String mensagemErro) throws IOException, SQLException {
        String titulo = pedido.getIDPagamento() == 0 ? "Novo pedido" : "Editar pedido #" + pedido.getIDPagamento();

        List<Cliente> clientes = clienteDAO.listar();
        clientes.sort(Comparator.comparing(Cliente::getNome, String.CASE_INSENSITIVE_ORDER));

        Resposta.html(troca, Layout.pagina(titulo, PedidoView.formulario(pedido, clientes,
                funcionarioDAO.listar(), mesaDAO.listar(), mensagemErro)));
    }

    private void editar(HttpExchange troca) throws IOException, SQLException {
        Pedido pedido = pedidoDAO.buscarPorId(numero(consulta(troca).get("id")));
        if (pedido == null) {
            Resposta.naoEncontrado(troca);
            return;
        }
        mostrarFormulario(troca, pedido, null);
    }

    private void ver(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> q = consulta(troca);
        int id = numero(q.get("id"));
        PedidoResumoDTO pedido = pedidoDAO.buscarResumo(id);
        if (pedido == null) {
            Resposta.naoEncontrado(troca);
            return;
        }
        Resposta.html(troca, Layout.pagina("Pedido #" + id,
                PedidoView.detalhe(pedido, itemDAO.listarPorPedido(id), pedidoDAO.listarProdutos(),
                        q.get("ok"), q.get("erro"))));
    }

    // ---------- ações do pedido ----------

    private void salvar(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);

        Pedido pedido = new Pedido();
        pedido.setIDPagamento(numero(form.get("id")));
        pedido.setIDCliente(numero(form.get("cliente")));
        pedido.setIDFuncionario(numero(form.get("funcionario")));
        pedido.setTipoConsumo(form.getOrDefault("tipo", "").trim());
        pedido.setFormaPagamento(form.getOrDefault("forma", "").trim());

        String mesa = form.getOrDefault("mesa", "").trim();
        pedido.setNumeroMesa(mesa.isEmpty() ? null : numero(mesa));
        if ("Para viagem".equals(pedido.getTipoConsumo())) {
            pedido.setNumeroMesa(null); // pedido para viagem não ocupa mesa
        }

        String erro = validar(pedido);
        if (erro != null) {
            mostrarFormulario(troca, pedido, erro);
            return;
        }

        try {
            if (pedido.getIDPagamento() == 0) {
                pedidoDAO.inserir(pedido);
            } else {
                if (pedidoDAO.buscarPorId(pedido.getIDPagamento()) == null) {
                    Resposta.naoEncontrado(troca);
                    return;
                }
                pedidoDAO.atualizar(pedido);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // FK de cliente, funcionário ou mesa apontando para algo que não existe
            mostrarFormulario(troca, pedido, "O cliente, o funcionário ou a mesa escolhida não existe.");
            return;
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_CHECK) {
                mostrarFormulario(troca, pedido, "O banco recusou os dados: confira tipo de consumo e forma de pagamento.");
                return;
            }
            throw e;
        }

        // novo pedido: vai direto para a página de itens
        redirecionar(troca, "/pedidos/ver?id=" + pedido.getIDPagamento(), null, null);
    }

    private void excluir(HttpExchange troca) throws IOException, SQLException {
        int id = numero(Formulario.lerCorpo(troca).get("id"));
        pedidoDAO.excluir(id);
        redirecionar(troca, "/pedidos", "Pedido #" + id + " excluído (os itens dele saíram em cascata).", null);
    }

    // chama a procedure sp_atualizar_status_pedido e mostra a mensagem que ela devolve
    private void avancarStatus(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);
        int id = numero(form.get("id"));
        String mensagem = pedidoDAO.atualizarStatus(id, form.getOrDefault("novo", "").trim());

        boolean deuCerto = mensagem != null && mensagem.startsWith("Status alterado");
        redirecionar(troca, "/pedidos/ver?id=" + id, deuCerto ? mensagem : null, deuCerto ? null : mensagem);
    }

    // ---------- ações dos itens ----------

    private void adicionarItem(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);
        int idPedido = numero(form.get("pedido"));
        int produto = numero(form.get("produto"));
        int quantidade = numero(form.get("quantidade"));
        String observacao = form.getOrDefault("observacao", "").trim();

        PedidoResumoDTO pedido = pedidoDAO.buscarResumo(idPedido);
        if (pedido == null) {
            Resposta.naoEncontrado(troca);
            return;
        }

        String caminho = "/pedidos/ver?id=" + idPedido;
        String erro = null;
        if (!"Pendente".equals(pedido.getStatus())) {
            erro = "Só é possível alterar os itens de pedidos pendentes.";
        } else if (produto <= 0) {
            erro = "Escolha um produto.";
        } else if (quantidade < 1 || quantidade > 999) {
            erro = "A quantidade deve estar entre 1 e 999.";
        } else if (observacao.length() > 200) {
            erro = "A observação não pode ter mais de 200 caracteres.";
        } else {
            erro = itemDAO.adicionar(idPedido, produto, quantidade, observacao.isEmpty() ? null : observacao);
        }

        redirecionar(troca, caminho, erro == null ? "Item adicionado. O total foi recalculado pelo trigger." : null, erro);
    }

    private void removerItem(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> form = Formulario.lerCorpo(troca);
        int idPedido = numero(form.get("pedido"));
        int item = numero(form.get("item"));

        PedidoResumoDTO pedido = pedidoDAO.buscarResumo(idPedido);
        if (pedido == null) {
            Resposta.naoEncontrado(troca);
            return;
        }

        String caminho = "/pedidos/ver?id=" + idPedido;
        if (!"Pendente".equals(pedido.getStatus())) {
            redirecionar(troca, caminho, null, "Só é possível alterar os itens de pedidos pendentes.");
            return;
        }
        itemDAO.remover(idPedido, item);
        redirecionar(troca, caminho, "Item removido. O total foi recalculado pelo trigger.", null);
    }

    // ---------- auxiliares ----------

    private String validar(Pedido p) {
        if (p.getIDCliente() <= 0) return "Escolha um cliente.";
        if (p.getIDFuncionario() <= 0) return "Escolha o funcionário que atendeu.";
        if (!Pedido.TIPOS_CONSUMO.contains(p.getTipoConsumo())) return "Tipo de consumo inválido.";
        if (!Pedido.FORMAS_PAGAMENTO.contains(p.getFormaPagamento())) return "Forma de pagamento inválida.";
        if (p.getNumeroMesa() != null && p.getNumeroMesa() <= 0) return "Mesa inválida.";
        return null;
    }

    // inteiro seguro: texto ausente ou inválido vira 0
    private static int numero(String texto) {
        if (texto == null) return 0;
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // parâmetros da URL, decodificados em UTF-8
    private static Map<String, String> consulta(HttpExchange troca) {
        Map<String, String> mapa = new HashMap<>();
        String bruta = troca.getRequestURI().getRawQuery();
        if (bruta == null) return mapa;
        for (String par : bruta.split("&")) {
            if (par.isEmpty()) continue;
            int i = par.indexOf('=');
            String chave = URLDecoder.decode(i < 0 ? par : par.substring(0, i), StandardCharsets.UTF_8);
            String valor = i < 0 ? "" : URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8);
            mapa.put(chave, valor);
        }
        return mapa;
    }

    // redireciona (POST/Redirect/GET) levando a mensagem na URL; assim, atualizar a página não repete a ação
    private static void redirecionar(HttpExchange troca, String caminho, String ok, String erro) throws IOException {
        StringBuilder destino = new StringBuilder(caminho);
        char separador = caminho.contains("?") ? '&' : '?';
        if (ok != null) {
            destino.append(separador).append("ok=").append(URLEncoder.encode(ok, StandardCharsets.UTF_8));
            separador = '&';
        }
        if (erro != null) {
            destino.append(separador).append("erro=").append(URLEncoder.encode(erro, StandardCharsets.UTF_8));
        }
        Formulario.redirecionar(troca, destino.toString());
    }
}