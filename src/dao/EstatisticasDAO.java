package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dto.EstatisticasDTO;
import main.Conexao;

public class EstatisticasDAO {
    public List<EstatisticasDTO> calcular() throws SQLException{
        List<EstatisticasDTO> lista = new ArrayList<>();
        String sql = "select count(*) as Quantidade, avg(valor_total) as Media, stddev_samp(valor_total) as Desvio_padrao, var_samp(valor_total) as Variancia from Pedido_pagamento";
        String moda = "";
        String sqlModa = "select Forma_pagamento as Moda from Pedido_pagamento " +
                 "group by Forma_pagamento order by count(*) desc limit 1";

        try (Connection conn = Conexao.getConexao();
            Statement st = conn.createStatement()){

            try (ResultSet rsModa = st.executeQuery(sqlModa)) {
                if (rsModa.next()) {
                    moda = rsModa.getString("Moda");
                }
            }

            try(ResultSet rs = st.executeQuery(sql)){
                while(rs.next()){
                    lista.add(new EstatisticasDTO(
                        rs.getInt("Quantidade"),
                        rs.getDouble("Media"),
                        rs.getDouble("Desvio_padrao"),
                        rs.getDouble("Variancia"),
                        moda
                    ));
                }

        }
            
            return lista;
        }
    }
}
