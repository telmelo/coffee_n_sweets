package dao;

import main.Conexao;
import model.Funcionario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {
    public void inserir(Funcionario f) throws SQLException {
        String sql = "INSERT INTO Funcionario (Nome, Cargo, Data_admissao, ID_funcionario_supervisor) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getNome());
            ps.setString(2, f.getCargo());
            ps.setDate(3, Date.valueOf(f.getDataAdmissao()));
            if (f.getIDSupervisor() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, f.getIDSupervisor());
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    f.setIDFuncionario(rs.getInt(1));
                }
            }
        }
    }

    public List<Funcionario> listar() throws SQLException {
        List<Funcionario> lista = new ArrayList<>();
        String sql = "SELECT * FROM Funcionario ORDER BY Nome";
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int supervisor = rs.getInt("ID_funcionario_supervisor");
                Integer idSupervisor = rs.wasNull() ? null : supervisor;
                lista.add(new Funcionario(
                        rs.getInt("ID_funcionario"),
                        rs.getString("Nome"),
                        rs.getString("Cargo"),
                        rs.getDate("Data_admissao").toLocalDate(),
                        idSupervisor));
            }
        }
        return lista;
    }

    public Funcionario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Funcionario WHERE ID_funcionario=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int supervisor = rs.getInt("ID_funcionario_supervisor");
                    Integer idSupervisor = rs.wasNull() ? null : supervisor;
                    return new Funcionario(
                            rs.getInt("ID_funcionario"),
                            rs.getString("Nome"),
                            rs.getString("Cargo"),
                            rs.getDate("Data_admissao").toLocalDate(),
                            idSupervisor
                    );
                }
            }
        }
        return null;
    }

    public void atualizar(Funcionario f) throws SQLException {
        String sql = "UPDATE Funcionario SET Nome=?, Cargo=?, Data_admissao=?, ID_funcionario_supervisor=? WHERE ID_funcionario=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNome());
            ps.setString(2, f.getCargo());
            ps.setDate(3, Date.valueOf(f.getDataAdmissao()));
            if (f.getIDSupervisor() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, f.getIDSupervisor());
            }
            ps.setInt(5, f.getIDFuncionario());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM Funcionario WHERE ID_funcionario=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}