package dto;

import java.time.LocalDateTime;

public class LogPrecoDTO {
    private int idLog;
    private String nomeProduto;
    private double precoAntigo;
    private double precoNovo;
    private LocalDateTime dataAlteracao;
    private String usuario;

    public LogPrecoDTO(int idLog, String nomeProduto, double precoAntigo, double precoNovo, 
        LocalDateTime dataAlteracao, String usuario){
            this.idLog = idLog;
            this.nomeProduto = nomeProduto;
            this.precoAntigo = precoAntigo;
            this.precoNovo = precoNovo;
            this.dataAlteracao = dataAlteracao;
            this.usuario = usuario;
        }

    public int getIdLog(){
        return idLog;
    }

    public String getNomeProduto(){
        return  nomeProduto;
    }

    public double getPrecoAntigo(){
        return precoAntigo;
    }

    public double getPrecoNovo(){
        return precoNovo;
    }

    public LocalDateTime getDataAlteracao(){
        return dataAlteracao;
    }

    public String getUsuario(){
        return usuario;
    }
}
