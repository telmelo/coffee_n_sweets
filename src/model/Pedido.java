package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Pedido {
    public static final List<String> TIPOS_CONSUMO = List.of("Aqui", "Para viagem");
    public static final List<String> STATUS = List.of("Pendente", "Em preparo", "Pronto");
    public static final List<String> FORMAS_PAGAMENTO = List.of("Débito", "Crédito", "Dinheiro");

    private int IDPagamento;
    private LocalDate data;
    private LocalTime horario;
    private String tipoConsumo = TIPOS_CONSUMO.get(0);
    private String status = STATUS.get(0);
    private int IDCliente;
    private int IDFuncionario;
    private Integer numeroMesa; // null: sem mesa (ex.: para viagem)
    private String formaPagamento = FORMAS_PAGAMENTO.get(0);
    private double valorTotal;

    public Pedido() {}

    public Pedido(int IDPagamento, LocalDate data, LocalTime horario, String tipoConsumo, String status,
                  int IDCliente, int IDFuncionario, Integer numeroMesa, String formaPagamento, double valorTotal) {
        this.IDPagamento = IDPagamento;
        this.data = data;
        this.horario = horario;
        this.tipoConsumo = tipoConsumo;
        this.status = status;
        this.IDCliente = IDCliente;
        this.IDFuncionario = IDFuncionario;
        this.numeroMesa = numeroMesa;
        this.formaPagamento = formaPagamento;
        this.valorTotal = valorTotal;
    }

    public int getIDPagamento() { return IDPagamento; }
    public void setIDPagamento(int IDPagamento) { this.IDPagamento = IDPagamento; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }

    public String getTipoConsumo() { return tipoConsumo; }
    public void setTipoConsumo(String tipoConsumo) { this.tipoConsumo = tipoConsumo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getIDCliente() { return IDCliente; }
    public void setIDCliente(int IDCliente) { this.IDCliente = IDCliente; }

    public int getIDFuncionario() { return IDFuncionario; }
    public void setIDFuncionario(int IDFuncionario) { this.IDFuncionario = IDFuncionario; }

    public Integer getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(Integer numeroMesa) { this.numeroMesa = numeroMesa; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
}