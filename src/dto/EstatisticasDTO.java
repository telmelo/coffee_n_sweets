package dto;

public class EstatisticasDTO {
    private int quantidade;
    private double media;
    private double desvioPadrao;
    private double variancia;
    private String moda;

    public EstatisticasDTO(int quantidade, double media, 
        double desvioPadrao, double variancia, String moda){
            this.quantidade = quantidade;
            this.media = media;
            this.desvioPadrao = desvioPadrao;
            this.variancia = variancia;
            this.moda = moda;
        }

    public int getQuantidade(){
        return quantidade;
    }

    public double getMedia(){
        return media;
    }

    public double getDesvioPadrao(){
        return desvioPadrao;
    }

    public double getVariancia(){
        return variancia;
    }

    public String getModa(){
        return moda;
    }
}
