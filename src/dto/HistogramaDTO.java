package dto;

public class HistogramaDTO {
    private String faixa;
    private int quantidade;

    public HistogramaDTO(String faixa, int quantidade){
        this.faixa = faixa;
        this.quantidade = quantidade;
    }

    public String getFaixa(){
        return faixa;
    }

    public int getQuantidade(){
        return quantidade;
    }
}
