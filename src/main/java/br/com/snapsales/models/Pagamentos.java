package br.com.snapsales.models;

/**
 *
 * @author auxiliar.tirs
 */
public class Pagamentos {
    private long id;
    private String formaPagamento;

    public Pagamentos(long id, String formaPagamento) {
        this.id = id;
        this.formaPagamento = formaPagamento;
    }

    public Pagamentos() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
}
