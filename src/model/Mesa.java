package model;

import java.util.List;

public class Mesa {
    public static final List<String> LOCALIZACOES = List.of("Salão interno", "Área externa");

    private int numero;
    private int capacidade;
    private String localizacao = LOCALIZACOES.get(0);

    public Mesa() {}

    public Mesa(int numero, int capacidade, String localizacao) {
        this.numero = numero;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
    }

    public int getNumero() {
        return numero;
    }
    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getCapacidade() {
        return capacidade;
    }
    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }
    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }
}
