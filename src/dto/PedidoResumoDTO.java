package dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class PedidoResumoDTO {
    private final int IDPagamento;
    private final LocalDate data;
    private final LocalTime horario;
    private final String tipoConsumo;
    private final String status;
    private final String cliente;
    private final String funcionario;
    private final Integer numeroMesa;
    private final String formaPagamento;
    private final double valorTotal;

    public PedidoResumoDTO(int IDPagamento, LocalDate data, LocalTime horario, String tipoConsumo, String status,
                           String cliente, String funcionario, Integer numeroMesa, String formaPagamento, double valorTotal) {
        this.IDPagamento = IDPagamento;
        this.data = data;
        this.horario = horario;
        this.tipoConsumo = tipoConsumo;
        this.status = status;
        this.cliente = cliente;
        this.funcionario = funcionario;
        this.numeroMesa = numeroMesa;
        this.formaPagamento = formaPagamento;
        this.valorTotal = valorTotal;
    }

    public int getIDPagamento() { return IDPagamento; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public String getTipoConsumo() { return tipoConsumo; }
    public String getStatus() { return status; }
    public String getCliente() { return cliente; }
    public String getFuncionario() { return funcionario; }
    public Integer getNumeroMesa() { return numeroMesa; }
    public String getFormaPagamento() { return formaPagamento; }
    public double getValorTotal() { return valorTotal; }
}