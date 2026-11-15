package dao;

import dto.*;
import main.Conexao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public List<ProdutosNaoVendidosDTO> produtosNaoVendidos() throws SQLException {
        List<ProdutosNaoVendidosDTO> lista = new ArrayList<>();
        String sql = "select p.ID_produto, p.Nome, p.Preco from Produto p left join Item_pedido ip on ip.ID_produto = p.ID_produto " +
                "where ip.ID_produto is null order by p.Nome;";

        try (Connection conn = Conexao.getConexao();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new ProdutosNaoVendidosDTO(
                        rs.getInt("ID_produto"),
                        rs.getString("Nome"),
                        rs.getDouble("Preco")
                ));
            }
        }
        return lista;
    }

    public List<RelacaoMesaPedidoDTO> relacaoMesaPedido() throws SQLException {
        List<RelacaoMesaPedidoDTO> lista = new ArrayList<>();
        String sql = "select m.Numero_mesa, m.Localizacao, pp.ID_pagamento, pp.Data, pp.Valor_total from Mesa m left join Pedido_pagamento pp on pp.Numero_mesa = m.Numero_mesa " +
                "union select m.Numero_mesa, m.Localizacao, pp.ID_pagamento, pp.Data, pp.Valor_total from Mesa m " +
                "right join Pedido_pagamento pp on pp.Numero_mesa = m.Numero_mesa order by Numero_mesa, ID_pagamento;";

        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int mesa = rs.getInt("Numero_mesa");
                Integer numeroMesa = rs.wasNull() ? null : mesa;

                String localizacao = rs.getString("Localizacao");

                int pagamento = rs.getInt("ID_pagamento");
                Integer idPagamento = rs.wasNull() ? null : pagamento;

                Date data = rs.getDate("Data");
                LocalDate dataPedido = data == null ? null : data.toLocalDate();

                double valor = rs.getDouble("Valor_total");
                Double valorTotal = rs.wasNull() ? null : valor;
                lista.add(new RelacaoMesaPedidoDTO(numeroMesa, localizacao, idPagamento, dataPedido, valorTotal));
            }
        }
        return lista;
    }

    public List<ClienteUltimoPedidoDTO> clientesUltimoPedido() throws SQLException {
        List<ClienteUltimoPedidoDTO> lista = new ArrayList<>();
        String sql = "select c.ID_cliente, c.Nome, pp.ID_pagamento, pp.Data as Data_ultimo_pedido, pp.Forma_pagamento, pp.Valor_total, datediff(curdate(), pp.Data) as Dias_sem_comprar from Pedido_pagamento pp " +
                "join Cliente c on c.ID_cliente = pp.ID_cliente where pp.Data = ( select max(pp2.Data) from Pedido_pagamento pp2 where pp2.ID_cliente = pp.ID_cliente ) " +
                "order by Dias_sem_comprar desc;";

        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new ClienteUltimoPedidoDTO(
                        rs.getInt("ID_cliente"),
                        rs.getString("Nome"),
                        rs.getInt("ID_pagamento"),
                        rs.getDate("Data_ultimo_pedido").toLocalDate(),
                        rs.getString("Forma_pagamento"),
                        rs.getInt("Dias_sem_comprar")
                ));
            }
        }
        return lista;
    }

    public List<ClienteCafeSemLancheDTO> clientesCafeSemLanche() throws SQLException {
        List<ClienteCafeSemLancheDTO> lista = new ArrayList<>();
        String sql = "select c.ID_cliente, c.Nome, c.Email from Cliente c where exists ( " +
                "select 1 from Pedido_pagamento pp join Item_pedido ip on ip.ID_pagamento = pp.ID_pagamento " +
                "join Cafe ca on ca.ID_produto = ip.ID_produto where pp.ID_cliente = c.ID_cliente ) and not exists ( " +
                "select 1 from Pedido_pagamento pp join Item_pedido ip on ip.ID_pagamento = pp.ID_pagamento " +
                "join Lanche la on la.ID_produto = ip.ID_produto where pp.ID_cliente = c.ID_cliente ) order by c.Nome;";

        try (Connection conn = Conexao.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new ClienteCafeSemLancheDTO(
                        rs.getInt("ID_cliente"),
                        rs.getString("Nome"),
                        rs.getString("Email")
                ));
            }
        }
        return lista;
    }
}
