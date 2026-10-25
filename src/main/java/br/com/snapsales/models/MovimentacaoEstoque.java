package br.com.snapsales.models;

/**
 * Uma linha de movimentacao: um produto, uma quantidade, entrada ou saida.
 * (tipoMovimentacao: true = entrada, false = saida)
 *
 * @author auxiliar.tirs
 */
public class MovimentacaoEstoque {

    private long id;
    private Produto produto;
    private int quantidade;
    private boolean tipoMovimentacao;
    private double precoUnitario;   // novo: preco do produto no momento da movimentacao

    public MovimentacaoEstoque(long id, Produto produto, int quantidade, boolean tipoMovimentacao) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public MovimentacaoEstoque() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public boolean isTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(boolean tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
}
