package dto;

import java.time.LocalDate;

public class ClienteUltimoPedidoDTO {
    private int IDCliente;
    private String nome;
    private int IDPagamento;
    private LocalDate dataUltimoPedido;
    private String formaPagamento;
    private int diasSemComprar;

    public ClienteUltimoPedidoDTO(int IDCliente, String nome, int IDPagamento, LocalDate dataUltimoPedido, String formaPagamento, int diasSemComprar) {
        this.IDCliente = IDCliente;
        this.nome = nome;
        this.IDPagamento = IDPagamento;
        this.dataUltimoPedido = dataUltimoPedido;
        this.formaPagamento = formaPagamento;
        this.diasSemComprar = diasSemComprar;
    }

    public int getIDCliente() {
        return IDCliente;
    }

    public String getNome() {
        return nome;
    }

    public int getIDPagamento() {
        return IDPagamento;
    }

    public LocalDate getDataUltimoPedido() {
        return dataUltimoPedido;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public int getDiasSemComprar() {
        return diasSemComprar;
    }
}
