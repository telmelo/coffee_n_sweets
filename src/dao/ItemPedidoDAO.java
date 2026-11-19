package dao;

import dto.ItemPedidoDTO;
import main.Conexao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemPedidoDAO {

    public List<ItemPedidoDTO> listarPorPedido(int idPagamento) throws SQLException {
        List<ItemPedidoDTO> lista = new ArrayList<>();
        String sql = """
                select i.Numero_item, p.Nome, i.Quantidade, i.Preco_unitario, i.Observacao
                  from Item_pedido i
                  join Produto p on p.ID_produto = i.ID_produto
                 where i.ID_pagamento = ?
                 order by i.Numero_item
                """;
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPagamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ItemPedidoDTO(
                            rs.getInt("Numero_item"),
                            rs.getString("Nome"),
                            rs.getInt("Quantidade"),
                            rs.getDouble("Preco_unitario"),
                            rs.getString("Observacao")
                    ));
                }
            }
        }
        return lista;
    }

    public String adicionar(int idPagamento, int idProduto, int quantidade, String observacao) throws SQLException {
        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false);
            try {
                // trava a linha do pedido: confirma que existe e evita dois itens com o mesmo número
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ID_pagamento FROM Pedido_pagamento WHERE ID_pagamento = ? FOR UPDATE")) {
                    ps.setInt(1, idPagamento);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "Pedido não encontrado.";
                        }
                    }
                }

                BigDecimal preco;
                try (PreparedStatement ps = conn.prepareStatement("SELECT Preco FROM Produto WHERE ID_produto = ?")) {
                    ps.setInt(1, idProduto);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "Produto não encontrado.";
                        }
                        preco = rs.getBigDecimal("Preco");
                    }
                }

                int proximo;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(MAX(Numero_item), 0) + 1 FROM Item_pedido WHERE ID_pagamento = ?")) {
                    ps.setInt(1, idPagamento);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        proximo = rs.getInt(1);
                    }
                }

                String sql = "INSERT INTO Item_pedido (Numero_item, Quantidade, Preco_unitario, Observacao, ID_pagamento, ID_produto) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, proximo);
                    ps.setInt(2, quantidade);
                    ps.setBigDecimal(3, preco);
                    ps.setString(4, observacao);
                    ps.setInt(5, idPagamento);
                    ps.setInt(6, idProduto);
                    ps.executeUpdate();
                }

                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void remover(int idPagamento, int numeroItem) throws SQLException {
        String sql = "DELETE FROM Item_pedido WHERE ID_pagamento = ? AND Numero_item = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPagamento);
            ps.setInt(2, numeroItem);
            ps.executeUpdate();
        }
    }
}