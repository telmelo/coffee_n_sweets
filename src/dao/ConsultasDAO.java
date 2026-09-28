package dao;

import dto.BaristasDestaqueDTO;
import dto.CafesEGraosDTO;
import dto.FaturamentoDTO;
import dto.TopProdutosDTO;
import main.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultasDAO {

    public List<FaturamentoDTO> faturamentoPorDia() throws SQLException {
        List<FaturamentoDTO> lista = new ArrayList<>();
        String sql = "select Data, SUM(Valor_total) as Faturamento, Forma_pagamento from Pedido_pagamento group by Data, Forma_pagamento";

        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new FaturamentoDTO(
                        rs.getDate("Data").toLocalDate(),
                        rs.getDouble("Faturamento"),
                        rs.getString("Forma_pagamento")
                ));
            }
        }
        return lista;
    }

    public List<TopProdutosDTO> top10ProdutosVendidos() throws SQLException {
        List<TopProdutosDTO> lista = new ArrayList<>();
        String sql = "select p.ID_produto, p.Nome, SUM(i.Quantidade) as Total_vendido from Item_pedido i " +
                "join Produto p on i.ID_produto = p.ID_produto group by p.ID_produto, p.Nome order by Total_vendido desc limit 10";

        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new TopProdutosDTO(
                        rs.getInt("ID_produto"),
                        rs.getString("Nome"),
                        rs.getInt("Total_vendido")
                ));
            }
        }
        return lista;
    }

    public List<BaristasDestaqueDTO> baristasAcimaDaMedia() throws SQLException {
        List<BaristasDestaqueDTO> lista = new ArrayList<>();
        String sql = "select f.ID_funcionario, f.Nome, COUNT(distinct p.Numero_item, p.ID_pagamento) as Itens_preparados from Prepara p " +
                "join Funcionario f on p.ID_funcionario = f.ID_funcionario where f.cargo = 'Barista' group by f.ID_funcionario, f.Nome having Itens_preparados > ( " +
                "select AVG(Itens_preparados2) from ( " +
                "select COUNT(distinct p2.Numero_item, p2.ID_pagamento) as Itens_preparados2 from Prepara p2 " +
                "join Funcionario f2 on p2.ID_funcionario = f2.ID_funcionario where f2.cargo = 'Barista' group by f2.ID_funcionario " +
                ") as Media_preparo " +
                ")";

        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new BaristasDestaqueDTO(
                        rs.getInt("ID_funcionario"),
                        rs.getString("Nome"),
                        rs.getInt("Itens_preparados")
                ));
            }
        }
        return lista;
    }

    public List<CafesEGraosDTO> cafesEGraos() throws SQLException {
        List<CafesEGraosDTO> lista = new ArrayList<>();
        String sql = "select p.Nome as Nome_cafe, g.Nome as Nome_grao, g.Origem, fo.Razao_social, fn.Preco_kg from Cafe c join Grao g on c.ID_grao = g.ID_grao join Fornece fn on g.ID_grao = fn.ID_grao " +
                "join Fornecedor fo on fn.ID_fornecedor = fo.ID_fornecedor join Produto p on c.ID_produto = p.ID_produto order by p.Nome";

        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new CafesEGraosDTO(
                        rs.getString("Nome_cafe"),
                        rs.getString("Nome_grao"),
                        rs.getString("Origem"),
                        rs.getString("Razao_social"),
                        rs.getDouble("Preco_kg")
                ));
            }
        }
        return lista;
    }
}
