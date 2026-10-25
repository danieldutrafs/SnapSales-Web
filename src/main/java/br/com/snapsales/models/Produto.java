package br.com.snapsales.models;

/**
 * Produto do estoque. Classe pura (sem anotacoes de banco).
 *
 * @author auxiliar.tirs
 */
public class Produto {

    private long id;
    private String nomeProduto;
    private int quantidade;
    private double preco;
    private Categoria categoria;
    private boolean ativo = true;

    public Produto(long id, String nomeProduto, int quantidade, double preco, Categoria categoria) {
        this.id = id;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.preco = preco;
        this.categoria = categoria;
    }

    public Produto() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
