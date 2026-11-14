package model;

import java.time.LocalDate;
import java.util.List;

public class Funcionario {
    public static final List<String> CARGOS = List.of("Atendente", "Barista", "Gerente");

    private int IDFuncionario;
    private String nome;
    private String cargo = CARGOS.get(0);
    private LocalDate dataAdmissao;
    private Integer IDSupervisor;

    public Funcionario() {}

    public Funcionario(int IDFuncionario, String nome, String cargo, LocalDate dataAdmissao, Integer IDSupervisor) {
        this.IDFuncionario = IDFuncionario;
        this.nome = nome;
        this.cargo = cargo;
        this.dataAdmissao = dataAdmissao;
        this.IDSupervisor = IDSupervisor;
    }

    public int getIDFuncionario() {
        return IDFuncionario;
    }
    public void setIDFuncionario(int IDFuncionario) {
        this.IDFuncionario = IDFuncionario;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }
    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public Integer getIDSupervisor() {
        return IDSupervisor;
    }
    public void setIDSupervisor(Integer IDSupervisor) {
        this.IDSupervisor = IDSupervisor;
    }
}