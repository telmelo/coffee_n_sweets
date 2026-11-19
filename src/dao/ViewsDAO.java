package dao;

import main.Conexao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ViewsDAO {
    public static final List<String> TIPOS_CONSUMO = List.of("Aqui", "Para viagem");

    public record VendaLinha(int pedido, int item, LocalDate data, String horario, String tipo, String forma,
                             String cliente, String atendente, String mesa, String produto,
                             int quantidade, double preco, double subtotal) {}

    public record ProducaoLinha(int idBarista, String barista, int idEquipamento, String equipamento,
                                int itens, int unidades, int produtos, LocalDate primeiro, LocalDate ultimo) {}

    public record Opcao(int id, String nome) {}

    /** vw_vendas_detalhadas filtrada por período, forma de pagamento e tipo de consumo (null = todos). */
    public List<VendaLinha> vendas(LocalDate inicio, LocalDate fim, String forma, String tipo) throws SQLException {
        String sql = """
                select * from vw_vendas_detalhadas
                 where Data_pedido between ? and ?
                   and (? is null or Forma_pagamento = ?)
                   and (? is null or Tipo_consumo = ?)
                 order by Data_pedido, Horario_pedido, ID_pagamento, Numero_item
                """;
        List<VendaLinha> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            ps.setString(3, forma);
            ps.setString(4, forma);
            ps.setString(5, tipo);
            ps.setString(6, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new VendaLinha(
                            rs.getInt("ID_pagamento"), rs.getInt("Numero_item"),
                            rs.getDate("Data_pedido").toLocalDate(), rs.getString("Horario_pedido"),
                            rs.getString("Tipo_consumo"), rs.getString("Forma_pagamento"),
                            rs.getString("Nome_cliente"), rs.getString("Nome_atendente"),
                            rs.getString("Mesa"), rs.getString("Nome_produto"),
                            rs.getInt("Quantidade_item"), rs.getDouble("Preco_unitario"),
                            rs.getDouble("Subtotal_item")));
                }
            }
        }
        return lista;
    }

    /** vw_producao_barista_equipamento filtrada por barista e equipamento (null = todos). */
    public List<ProducaoLinha> producao(Integer idBarista, Integer idEquipamento) throws SQLException {
        String sql = """
                select * from vw_producao_barista_equipamento
                 where (? is null or ID_funcionario = ?)
                   and (? is null or ID_equipamento = ?)
                 order by Nome_barista, Nome_equipamento
                """;
        List<ProducaoLinha> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            definirInteiro(ps, 1, idBarista);
            definirInteiro(ps, 2, idBarista);
            definirInteiro(ps, 3, idEquipamento);
            definirInteiro(ps, 4, idEquipamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ProducaoLinha(
                            rs.getInt("ID_funcionario"), rs.getString("Nome_barista"),
                            rs.getInt("ID_equipamento"), rs.getString("Nome_equipamento"),
                            rs.getInt("Itens_distintos_preparados"), rs.getInt("Unidades_produzidas"),
                            rs.getInt("Produtos_diferentes"),
                            rs.getDate("Primeiro_preparo").toLocalDate(),
                            rs.getDate("Ultimo_preparo").toLocalDate()));
                }
            }
        }
        return lista;
    }

    public List<Opcao> baristas() throws SQLException {
        return opcoes("select ID_funcionario, Nome from Funcionario where Cargo = 'Barista' order by Nome");
    }

    public List<Opcao> equipamentos() throws SQLException {
        return opcoes("select ID_equipamento, Nome from Equipamento order by Nome");
    }

    private List<Opcao> opcoes(String sql) throws SQLException {
        List<Opcao> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(new Opcao(rs.getInt(1), rs.getString(2)));
        }
        return lista;
    }

    private static void definirInteiro(PreparedStatement ps, int indice, Integer valor) throws SQLException {
        if (valor == null) ps.setNull(indice, Types.INTEGER);
        else ps.setInt(indice, valor);
    }
}