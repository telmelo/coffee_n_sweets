package dao;

import dto.TopProdutosDTO;
import main.Conexao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {
    public static final List<String> FORMAS_PAGAMENTO = List.of("Crédito", "Débito", "Dinheiro");

    public record PedidoLinha(int id, LocalDate data, String forma, double valor, String tipoConsumo) {}

    public LocalDate[] intervaloDatas() throws SQLException {
        String sql = "select min(Data) as Inicio, max(Data) as Fim from Pedido_pagamento";
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next() && rs.getDate("Inicio") != null) {
                return new LocalDate[]{rs.getDate("Inicio").toLocalDate(), rs.getDate("Fim").toLocalDate()};
            }
        }
        LocalDate hoje = LocalDate.now();
        return new LocalDate[]{hoje, hoje};
    }

    public List<PedidoLinha> pedidos(LocalDate inicio, LocalDate fim, String forma) throws SQLException {
        List<PedidoLinha> lista = new ArrayList<>();
        String sql = """
                select ID_pagamento, Data, Forma_pagamento, Valor_total, Tipo_consumo
                  from Pedido_pagamento
                 where Data between ? and ?
                   and (? is null or Forma_pagamento = ?)
                 order by Data, ID_pagamento
                """;

        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            ps.setString(3, forma);
            ps.setString(4, forma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new PedidoLinha(
                            rs.getInt("ID_pagamento"),
                            rs.getDate("Data").toLocalDate(),
                            rs.getString("Forma_pagamento"),
                            rs.getDouble("Valor_total"),
                            rs.getString("Tipo_consumo")
                    ));
                }
            }
        }
        return lista;
    }

    public List<TopProdutosDTO> topProdutos(LocalDate inicio, LocalDate fim, String forma) throws SQLException {
        List<TopProdutosDTO> lista = new ArrayList<>();
        String sql = """
                select p.ID_produto, p.Nome, SUM(i.Quantidade) as Total_vendido
                  from Item_pedido i
                  join Pedido_pagamento pg on pg.ID_pagamento = i.ID_pagamento
                  join Produto p on p.ID_produto = i.ID_produto
                 where pg.Data between ? and ?
                   and (? is null or pg.Forma_pagamento = ?)
                 group by p.ID_produto, p.Nome
                 order by Total_vendido desc, p.Nome
                 limit 10
                """;

        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            ps.setString(3, forma);
            ps.setString(4, forma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new TopProdutosDTO(
                            rs.getInt("ID_produto"),
                            rs.getString("Nome"),
                            rs.getInt("Total_vendido")
                    ));
                }
            }
        }
        return lista;
    }
}