package dto;

public class ItemPedidoDTO {
    private final int numeroItem;
    private final String produto;
    private final int quantidade;
    private final double precoUnitario;
    private final String observacao;

    public ItemPedidoDTO(int numeroItem, String produto, int quantidade, double precoUnitario, String observacao) {
        this.numeroItem = numeroItem;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.observacao = observacao;
    }

    public int getNumeroItem() { return numeroItem; }
    public String getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public String getObservacao() { return observacao; }
    public double getSubtotal() { return quantidade * precoUnitario; }
}