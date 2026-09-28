package dao;

import model.Produto;
import main.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {
    public void inserir(Produto p) throws SQLException {
        String sql = "INSERT INTO Produto (Nome, Preco, Descricao) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNome());
            ps.setFloat(2, p.getPreco());
            ps.setString(3, p.getDescricao());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setIDProduto(rs.getInt(1));
                }
            }
        }
    }

    public List<Produto> listar() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM Produto";
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Produto(
                        rs.getInt("ID_produto"),
                        rs.getString("Nome"),
                        rs.getFloat("Preco"),
                        rs.getString("Descricao")
                ));
            }
        }
        return lista;
    }

    /** Busca um produto pelo ID; devolve null se não existir. */
    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Produto WHERE ID_produto=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Produto(
                            rs.getInt("ID_produto"),
                            rs.getString("Nome"),
                            rs.getFloat("Preco"),
                            rs.getString("Descricao")
                    );
                }
            }
        }
        return null;
    }

    public void atualizar(Produto p) throws SQLException {
        String sql = "UPDATE Produto SET Nome=?, Preco=?, Descricao=? WHERE ID_produto=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setFloat(2, p.getPreco());
            ps.setString(3, p.getDescricao());
            ps.setInt(4, p.getIDProduto());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM Produto WHERE ID_produto=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}