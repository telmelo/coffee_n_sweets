package main;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import dao.ClienteDAO;
import dao.EstatisticasDAO;
import dao.ProdutoDAO;
import dto.EstatisticasDTO;
import model.Cliente;
import model.Produto;

public class TesteDAO {
    public static void main(String[] args) {
        ClienteDAO clienteDAO = new ClienteDAO();
        ProdutoDAO produtoDAO = new ProdutoDAO();

        try {
            Cliente novo = new Cliente(0, "João Silva", "joao@email.com", LocalDate.now());
            clienteDAO.inserir(novo);
            System.out.println("Cliente inserido! ID gerado: " + novo.getIDCliente());

            List<Cliente> clientes = clienteDAO.listar();
            System.out.println("\n--- Lista de clientes ---");
            for (Cliente c : clientes) {
                System.out.println(c.getIDCliente() + " | " + c.getNome() + " | "
                        + c.getEmail() + " | " + c.getDataCadastro());
            }

            novo.setNome("João Silva Atualizado");
            clienteDAO.atualizar(novo);
            System.out.println("\nCliente atualizado!");

            System.out.println("\n--- Lista após atualização ---");
            for (Cliente c : clienteDAO.listar()) {
                System.out.println(c.getIDCliente() + " | " + c.getNome());
            }

            clienteDAO.excluir(novo.getIDCliente());
            System.out.println("\nCliente excluído!");

            Produto novoProduto = new Produto(0, "Bolo de Café", 12.90f, "Bolo caseiro feito com café espresso");
            produtoDAO.inserir(novoProduto);
            System.out.println("\nProduto inserido! ID gerado: " + novoProduto.getIDProduto());

            List<Produto> produtos = produtoDAO.listar();
            System.out.println("\n--- Lista de produtos ---");
            for (Produto p : produtos) {
                System.out.println(p.getIDProduto() + " | " + p.getNome() + " | "
                        + p.getPreco() + " | " + p.getDescricao());
            }

            novoProduto.setPreco(15.90f);
            novoProduto.setDescricao("Bolo caseiro feito com café espresso com cobertura de chocolate");
            produtoDAO.atualizar(novoProduto);
            System.out.println("\nProduto atualizado!");

            System.out.println("\n--- Lista após atualização ---");
            for (Produto p : produtoDAO.listar()) {
                System.out.println(p.getIDProduto() + " | " + p.getNome() + " | " + p.getPreco() + " | " + p.getDescricao());
            }

            produtoDAO.excluir(novoProduto.getIDProduto());
            System.out.println("\nProduto excluído!");

            EstatisticasDAO dao = new EstatisticasDAO();
            List<EstatisticasDTO> lista = dao.calcular();
            EstatisticasDTO e = lista.get(0);

            System.out.println("Quantidade: " + e.getQuantidade());
            System.out.println("Média: "      + e.getMedia());
            System.out.println("Desvio: "     + e.getDesvioPadrao());
            System.out.println("Variância: "  + e.getVariancia());
            System.out.println("Moda: "       + e.getModa());

        } catch (SQLException e) {
            System.out.println("Erro no teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}