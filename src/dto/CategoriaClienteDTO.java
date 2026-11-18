package dto;

public class CategoriaClienteDTO {
    private String categoria;
    private int quantidade;

    public CategoriaClienteDTO(String categoria, int quantidade) {
        this.categoria = categoria;
        this.quantidade = quantidade;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
