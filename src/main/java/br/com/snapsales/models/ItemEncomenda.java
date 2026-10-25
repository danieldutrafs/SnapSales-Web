package br.com.snapsales.models;

/**
 *
 * @author auxiliar.tirs
 */
public class ItemEncomenda {
    private long id;
    private Encomenda encomenda;
    private Produto produto;
    private int quantidadeItem;
    private double precoMomento;

    public ItemEncomenda(long id, int quantidadeItem, double precoMomento) {
        this.id = id;
        this.quantidadeItem = quantidadeItem;
        this.precoMomento = precoMomento;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public void setEncomenda(Encomenda encomenda) {
        this.encomenda = encomenda;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidadeItem() {
        return quantidadeItem;
    }

    public void setQuantidadeItem(int quantidadeItem) {
        this.quantidadeItem = quantidadeItem;
    }

    public double getPrecoMomento() {
        return precoMomento;
    }

    public void setPrecoMomento(double precoMomento) {
        this.precoMomento = precoMomento;
    }    
}
