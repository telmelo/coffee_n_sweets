package dao;

import dto.PedidoResumoDTO;
import main.Conexao;
import model.Pedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {
    public record OpcaoProduto(int id, String nome, double preco) {}

    private static final String SELECT_RESUMO = """
            select pp.ID_pagamento, pp.Data, pp.Horario, pp.Tipo_consumo, pp.Status,
                   c.Nome as Cliente, f.Nome as Funcionario, pp.Numero_mesa,
                   pp.Forma_pagamento, pp.Valor_total
              from Pedido_pagamento pp
              join Cliente c on c.ID_cliente = pp.ID_cliente
              join Funcionario f on f.ID_funcionario = pp.ID_funcionario
            """;

    public void inserir(Pedido p) throws SQLException {
        String sql = "INSERT INTO Pedido_pagamento (Tipo_consumo, ID_cliente, ID_funcionario, Numero_mesa, Forma_pagamento) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, p);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setIDPagamento(rs.getInt(1));
                }
            }
        }
    }

    public List<PedidoResumoDTO> listarResumos(String status) throws SQLException {
        List<PedidoResumoDTO> lista = new ArrayList<>();
        String sql = SELECT_RESUMO
                + " where (? is null or pp.Status = ?) order by pp.Data desc, pp.Horario desc, pp.ID_pagamento desc";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(lerResumo(rs));
                }
            }
        }
        return lista;
    }

    public PedidoResumoDTO buscarResumo(int id) throws SQLException {
        String sql = SELECT_RESUMO + " where pp.ID_pagamento = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerResumo(rs);
                }
            }
        }
        return null;
    }

    public Pedido buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Pedido_pagamento WHERE ID_pagamento=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int mesa = rs.getInt("Numero_mesa");
                    Integer numeroMesa = rs.wasNull() ? null : mesa;
                    return new Pedido(
                            rs.getInt("ID_pagamento"),
                            rs.getDate("Data").toLocalDate(),
                            rs.getTime("Horario").toLocalTime(),
                            rs.getString("Tipo_consumo"),
                            rs.getString("Status"),
                            rs.getInt("ID_cliente"),
                            rs.getInt("ID_funcionario"),
                            numeroMesa,
                            rs.getString("Forma_pagamento"),
                            rs.getDouble("Valor_total")
                    );
                }
            }
        }
        return null;
    }

    public void atualizar(Pedido p) throws SQLException {
        String sql = "UPDATE Pedido_pagamento SET Tipo_consumo=?, ID_cliente=?, ID_funcionario=?, Numero_mesa=?, "
                + "Forma_pagamento=? WHERE ID_pagamento=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            preencher(ps, p);
            ps.setInt(6, p.getIDPagamento());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM Pedido_pagamento WHERE ID_pagamento=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<OpcaoProduto> listarProdutos() throws SQLException {
        List<OpcaoProduto> lista = new ArrayList<>();
        String sql = "SELECT ID_produto, Nome, Preco FROM Produto ORDER BY Nome";
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new OpcaoProduto(rs.getInt("ID_produto"), rs.getString("Nome"), rs.getDouble("Preco")));
            }
        }
        return lista;
    }

    public String atualizarStatus(int id, String novoStatus) throws SQLException {
        try (Connection conn = Conexao.getConexao();
             CallableStatement cs = conn.prepareCall("{call sp_atualizar_status_pedido(?, ?, ?)}")) {
            cs.setInt(1, id);
            cs.setString(2, novoStatus);
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.execute();
            return cs.getString(3);
        }
    }

    private void preencher(PreparedStatement ps, Pedido p) throws SQLException {
        ps.setString(1, p.getTipoConsumo());
        ps.setInt(2, p.getIDCliente());
        ps.setInt(3, p.getIDFuncionario());
        if (p.getNumeroMesa() == null) {
            ps.setNull(4, Types.INTEGER);
        } else {
            ps.setInt(4, p.getNumeroMesa());
        }
        ps.setString(5, p.getFormaPagamento());
    }

    private PedidoResumoDTO lerResumo(ResultSet rs) throws SQLException {
        int mesa = rs.getInt("Numero_mesa");
        Integer numeroMesa = rs.wasNull() ? null : mesa;
        return new PedidoResumoDTO(
                rs.getInt("ID_pagamento"),
                rs.getDate("Data").toLocalDate(),
                rs.getTime("Horario").toLocalTime(),
                rs.getString("Tipo_consumo"),
                rs.getString("Status"),
                rs.getString("Cliente"),
                rs.getString("Funcionario"),
                numeroMesa,
                rs.getString("Forma_pagamento"),
                rs.getDouble("Valor_total")
        );
    }
}