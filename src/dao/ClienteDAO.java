package dao;

import model.Cliente;
import main.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    public void inserir(Cliente c) throws SQLException {
        String sql = "INSERT INTO Cliente (Nome, Email, Data_cadastro) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setDate(3, Date.valueOf(c.getDataCadastro()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setIDCliente(rs.getInt(1));
                }
            }
        }
    }

    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Cliente";
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Cliente(
                        rs.getInt("ID_cliente"),
                        rs.getString("Nome"),
                        rs.getString("Email"),
                        rs.getDate("Data_cadastro").toLocalDate()
                ));
            }
        }
        return lista;
    }

    /** Busca um cliente pelo ID; devolve null se não existir. */
    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Cliente WHERE ID_cliente=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(
                            rs.getInt("ID_cliente"),
                            rs.getString("Nome"),
                            rs.getString("Email"),
                            rs.getDate("Data_cadastro").toLocalDate()
                    );
                }
            }
        }
        return null;
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = "UPDATE Cliente SET Nome=?, Email=?, Data_cadastro=? WHERE ID_cliente=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setDate(3, Date.valueOf(c.getDataCadastro()));
            ps.setInt(4, c.getIDCliente());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM Cliente WHERE ID_cliente=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}