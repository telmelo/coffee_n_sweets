package dao;

import dto.PedidoStatusDTO;
import dto.PreparoDTO;
import main.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProcedimentosDAO {

    public List<PedidoStatusDTO> pedidos() throws SQLException {
        List<PedidoStatusDTO> lista = new ArrayList<>();
        String sql = "select p.ID_pagamento, p.Data, p.Status, p.Valor_total, "
                + "(select count(*) from Item_pedido i where i.ID_pagamento = p.ID_pagamento) as Itens, "
                + "(select count(*) from Prepara pr where pr.ID_pagamento = p.ID_pagamento) as Atribuidos "
                + "from Pedido_pagamento p "
                + "order by field(p.Status, 'Pendente', 'Em preparo', 'Pronto'), p.ID_pagamento desc limit 15";
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new PedidoStatusDTO(rs.getInt("ID_pagamento"), rs.getDate("Data").toLocalDate(),
                        rs.getString("Status"), rs.getDouble("Valor_total"),
                        rs.getInt("Itens"), rs.getInt("Atribuidos")));
            }
        }
        return lista;
    }

    public List<PreparoDTO> preparosEmAndamento() throws SQLException {
        List<PreparoDTO> lista = new ArrayList<>();
        String sql = "select pr.ID_pagamento, pr.Numero_item, pd.Nome as Produto, "
                + "f.Nome as Barista, e.Nome as Equipamento "
                + "from Prepara pr "
                + "join Pedido_pagamento p on p.ID_pagamento = pr.ID_pagamento "
                + "join Item_pedido i on i.ID_pagamento = pr.ID_pagamento and i.Numero_item = pr.Numero_item "
                + "join Produto pd on pd.ID_produto = i.ID_produto "
                + "join Funcionario f on f.ID_funcionario = pr.ID_funcionario "
                + "join Equipamento e on e.ID_equipamento = pr.ID_equipamento "
                + "where p.Status = 'Em preparo' "
                + "order by pr.ID_pagamento desc, pr.Numero_item";
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new PreparoDTO(rs.getInt("ID_pagamento"), rs.getInt("Numero_item"),
                        rs.getString("Produto"), rs.getString("Barista"), rs.getString("Equipamento")));
            }
        }
        return lista;
    }

    public String atualizarStatus(int idPagamento, String novoStatus) throws SQLException {
        try (Connection conn = Conexao.getConexao()) {
            try (PreparedStatement ps = conn.prepareStatement("call sp_atualizar_status_pedido(?, ?, @msg_status)")) {
                ps.setInt(1, idPagamento);
                ps.setString(2, novoStatus);
                ps.execute();
            }
            return lerVariavelTexto(conn, "@msg_status");
        }
    }

    public int distribuirItensPendentes() throws SQLException {
        try (Connection conn = Conexao.getConexao()) {
            try (Statement st = conn.createStatement()) {
                st.execute("call sp_distribuir_itens_pendentes(@qtd_itens)");
            }
            String qtd = lerVariavelTexto(conn, "@qtd_itens");
            return qtd == null ? 0 : Integer.parseInt(qtd);
        }
    }

    private String lerVariavelTexto(Connection conn, String variavel) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select " + variavel)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }
}
