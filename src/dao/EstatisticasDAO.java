package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dto.CategoriaClienteDTO;
import dto.EstatisticasDTO;
import dto.HistogramaDTO;
import main.Conexao;

public class EstatisticasDAO {
    public List<EstatisticasDTO> calcular() throws SQLException{
        List<EstatisticasDTO> lista = new ArrayList<>();
        String sql = "select count(*) as Quantidade, avg(valor_total) as Media, stddev_samp(valor_total) as Desvio_padrao, var_samp(valor_total) as Variancia from Pedido_pagamento";
        String moda = "";
        String sqlModa = "select Forma_pagamento as Moda from Pedido_pagamento " +
                 "group by Forma_pagamento order by count(*) desc limit 1";
        double mediana = 0;
        String sqlMediana = """
                select distinct median(Valor_total) over () as Mediana
                from Pedido_pagamento
                """;

        try (Connection conn = Conexao.getConexao();
            Statement st = conn.createStatement()){

            try (ResultSet rsModa = st.executeQuery(sqlModa)) {
                if (rsModa.next()) {
                    moda = rsModa.getString("Moda");
                }
            }

            try (ResultSet rsMediana = st.executeQuery(sqlMediana)) {
                if (rsMediana.next()) {
                    mediana = rsMediana.getDouble("Mediana");
                }
            }

            try(ResultSet rs = st.executeQuery(sql)){
                while(rs.next()){
                    lista.add(new EstatisticasDTO(
                        rs.getInt("Quantidade"),
                        rs.getDouble("Media"),
                        rs.getDouble("Desvio_padrao"),
                        rs.getDouble("Variancia"),
                        moda,
                        mediana
                    ));
                }

        }
            
            return lista;
        }
    }

    public List<HistogramaDTO> histogramaValores() throws SQLException{
        List<HistogramaDTO> listaHistograma = new ArrayList<>();
        String sql = "select\n" + //
                        "    case\n" + //
                        "        when Valor_total < 20 then '0 a 20'\n" + //
                        "        when Valor_total < 40 then '20 a 40'\n" + //
                        "        when Valor_total < 60 then '40 a 60'\n" + //
                        "        else '60 ou mais'\n" + //
                        "    end as Faixa,\n" + //
                        "    count(*) as Quantidade\n" + //
                        "from Pedido_pagamento\n" + //
                        "group by Faixa\n" + //
                        "order by min(Valor_total);";

        try (Connection conn = Conexao.getConexao();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)){

                while(rs.next()){
                    listaHistograma.add(new HistogramaDTO(
                        rs.getString("Faixa"),
                        rs.getInt("Quantidade")
                    ));
                }

        }

        return listaHistograma;
    }

    public List<CategoriaClienteDTO> distribuicaoCategorias() throws SQLException {
        List<CategoriaClienteDTO> lista = new ArrayList<>();
        // o retorno da função vem com a collation do banco (utf8mb4_unicode_ci) e os literais
        // com a da conexão; sem o collate explícito o field() falha com "Illegal mix of collations"
        String sql = """
                select fn_categoria_cliente(ID_cliente) as Categoria, count(*) as Quantidade
                from Cliente
                group by Categoria
                order by field(Categoria collate utf8mb4_unicode_ci, 'Ouro', 'Prata', 'Bronze', 'Novo')
                """;

        try (Connection conn = Conexao.getConexao();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new CategoriaClienteDTO(
                    rs.getString("Categoria"),
                    rs.getInt("Quantidade")
                ));
            }
        }

        return lista;
    }
}
