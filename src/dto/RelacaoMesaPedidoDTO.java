package dto;

import java.time.LocalDate;

public class RelacaoMesaPedidoDTO {
    private Integer numeroMesa;
    private String localizacao;
    private Integer IDPagamento;
    private LocalDate data;
    private Double valorTotal;

    public RelacaoMesaPedidoDTO(Integer numeroMesa, String localizacao, Integer IDPagamento, LocalDate data, Double valorTotal) {
        this.numeroMesa = numeroMesa;
        this.localizacao = localizacao;
        this.IDPagamento = IDPagamento;
        this.data = data;
        this.valorTotal = valorTotal;
    }

    public Integer getNumeroMesa() {
        return numeroMesa;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public Integer getIDPagamento() {
        return IDPagamento;
    }

    public LocalDate getData() {
        return data;
    }

    public Double getValorTotal() {
        return valorTotal;
    }
}
