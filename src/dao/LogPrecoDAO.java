package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dto.LogPrecoDTO;
import main.Conexao;

public class LogPrecoDAO {
    public List<LogPrecoDTO> listarAlteracoesPreco() throws SQLException{
        List<LogPrecoDTO> lista = new ArrayList<>();
        String sql = """
        select l.ID_log, p.Nome, l.Preco_antigo, l.Preco_novo,
               l.Data_alteracao, l.Usuario
        from Log_alteracao_preco l
        join Produto p on p.ID_produto = l.ID_produto
        order by l.Data_alteracao desc
        """;

        try (Connection conn = Conexao.getConexao();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)){
                while(rs.next()){
                    lista.add(new LogPrecoDTO(
                        rs.getInt("Id_log"),
                        rs.getString("Nome"),
                        rs.getDouble("Preco_antigo"),
                        rs.getDouble("Preco_novo"),
                        rs.getTimestamp("Data_alteracao").toLocalDateTime(),
                        rs.getString("Usuario")
                    ));


                }
            }
        return lista;
    }
}
