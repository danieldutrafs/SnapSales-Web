package br.com.snapsales.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Movimentacao manual de estoque: um "cabecalho" (data, descricao, entrada ou saida)
 * com varias linhas (MovimentacaoEstoque), uma por produto.
 */
public class Movimentacao {

    private long id;
    private boolean entrada;
    private LocalDate data;
    private String descricao;
    private List<MovimentacaoEstoque> itens = new ArrayList<>();

    public Movimentacao() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEntrada() {
        return entrada;
    }

    public void setEntrada(boolean entrada) {
        this.entrada = entrada;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<MovimentacaoEstoque> getItens() {
        return itens;
    }

    public void setItens(List<MovimentacaoEstoque> itens) {
        this.itens = itens;
    }
}
