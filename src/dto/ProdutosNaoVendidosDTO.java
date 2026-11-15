package dto;

public class ProdutosNaoVendidosDTO {
    private int IDProduto;
    private String nome;
    private double preco;

    public ProdutosNaoVendidosDTO(int IDProduto, String nome, double preco) {
        this.IDProduto = IDProduto;
        this.nome = nome;
        this.preco = preco;
    }

    public int getIDProduto() {
        return IDProduto;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }
}
