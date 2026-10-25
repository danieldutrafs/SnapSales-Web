package br.com.snapsales.models;

import java.time.LocalDate;
import java.util.List;

/**
 * Encomenda de um cliente. Classe pura (sem anotacoes de banco).
 *
 * Status possiveis: "preparacao", "pronto" e "entregue".
 *
 * @author auxiliar.tirs
 */
public class Encomenda {

    public static final String PREPARACAO = "preparacao";
    public static final String PRONTO = "pronto";
    public static final String ENTREGUE = "entregue";

    private long id;
    private String status;
    private Clientes cliente;
    private double valorEntrada;
    private double valorTotal;
    private boolean retirada;
    private Entrega entrega;
    private Pagamentos pagamento;
    private List<ItemEncomenda> itens;

    // Novos atributos (vindos das telas)
    private LocalDate dataEntrega;
    private String horaEntrega;      // formato HH:mm
    private String destinatario;
    private String observacao;
    private LocalDate criadoEm;

    public Encomenda(long id, String status, Clientes cliente, double valorEntrada, double valorTotal, boolean retirada) {
        this.id = id;
        this.status = status;
        this.cliente = cliente;
        this.valorEntrada = valorEntrada;
        this.valorTotal = valorTotal;
        this.retirada = retirada;
    }

    public Encomenda() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Clientes getCliente() {
        return cliente;
    }

    public void setCliente(Clientes cliente) {
        this.cliente = cliente;
    }

    public double getValorEntrada() {
        return valorEntrada;
    }

    public void setValorEntrada(double valorEntrada) {
        this.valorEntrada = valorEntrada;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public boolean isRetirada() {
        return retirada;
    }

    public void setRetirada(boolean retirada) {
        this.retirada = retirada;
    }

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }

    public Pagamentos getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamentos pagamento) {
        this.pagamento = pagamento;
    }

    public List<ItemEncomenda> getItens() {
        return itens;
    }

    public void setItens(List<ItemEncomenda> itens) {
        this.itens = itens;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public String getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(String horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDate getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDate criadoEm) {
        this.criadoEm = criadoEm;
    }
}
