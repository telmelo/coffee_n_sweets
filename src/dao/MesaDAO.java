package dao;

import main.Conexao;
import model.Mesa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MesaDAO {
    public void inserir(Mesa m) throws SQLException {
        String sql = "INSERT INTO Mesa (Numero_mesa, Capacidade, Localizacao) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getNumero());
            ps.setInt(2, m.getCapacidade());
            ps.setString(3, m.getLocalizacao());
            ps.executeUpdate();
        }
    }

    public List<Mesa> listar() throws SQLException {
        List<Mesa> lista = new ArrayList<>();
        String sql = "SELECT * FROM Mesa ORDER BY Numero_mesa";
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Mesa(
                        rs.getInt("Numero_mesa"),
                        rs.getInt("Capacidade"),
                        rs.getString("Localizacao")
                ));
            }
        }
        return lista;
    }

    public Mesa buscarPorNumero(int numero) throws SQLException {
        String sql = "SELECT * FROM Mesa WHERE Numero_mesa=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Mesa(
                            rs.getInt("Numero_mesa"),
                            rs.getInt("Capacidade"),
                            rs.getString("Localizacao")
                    );
                }
            }
        }
        return null;
    }

    public void atualizar(Mesa m) throws SQLException {
        String sql = "UPDATE Mesa SET Capacidade=?, Localizacao=? WHERE Numero_mesa=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getCapacidade());
            ps.setString(2, m.getLocalizacao());
            ps.setInt(3, m.getNumero());
            ps.executeUpdate();
        }
    }

    public void excluir(int numero) throws SQLException {
        String sql = "DELETE FROM Mesa WHERE Numero_mesa=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numero);
            ps.executeUpdate();
        }
    }
}
