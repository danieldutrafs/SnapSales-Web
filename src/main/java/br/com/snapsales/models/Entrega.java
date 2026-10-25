package br.com.snapsales.models;

import java.time.LocalDate;

/**
 *
 * @author auxiliar.tirs
 */
public class Entrega {
    private long id;
    private String endereco;
    private String destinatario;
    private double frete;
    private LocalDate dataEntrega;

    public Entrega(long id, String endereco, String destinatario, double frete, LocalDate dataEntrega) {
        this.id = id;
        this.endereco = endereco;
        this.destinatario = destinatario;
        this.frete = frete;
        this.dataEntrega = dataEntrega;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public double getFrete() {
        return frete;
    }

    public void setFrete(double frete) {
        this.frete = frete;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega = dataEntrega;
    }
}
