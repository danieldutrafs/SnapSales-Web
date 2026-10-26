package br.com.snapsales.controllers;

import br.com.snapsales.models.Clientes;
import br.com.snapsales.models.Encomenda;
import br.com.snapsales.models.Entrega;
import br.com.snapsales.models.ItemEncomenda;
import br.com.snapsales.models.Pagamentos;
import br.com.snapsales.models.Produto;
import java.util.ArrayList;
import java.util.List;

/** Formato JSON de encomenda trocado com as telas. */
public record EncomendaDTO(Long id, String codigo, Long clienteId, String cliente, String telefone,
                           List<ItemEncomendaDTO> itens, Boolean retirada, String endereco,
                           String destinatario, String dataEntrega, String horaEntrega,
                           String observacao, String pagamento, Double frete, Double entrada,
                           String status, String criadoEm, Double valorTotal) {

    public static EncomendaDTO de(Encomenda e) {
        List<ItemEncomendaDTO> itens = new ArrayList<>();
        for (ItemEncomenda i : e.getItens()) {
            itens.add(new ItemEncomendaDTO(i.getProduto().getId(), i.getProduto().getNomeProduto(),
                    i.getQuantidadeItem(), i.getPrecoMomento()));
        }
        boolean retirada = e.isRetirada() || e.getEntrega() == null;
        return new EncomendaDTO(
                e.getId(),
                Formatos.codigo(e.getId()),
                e.getCliente().getId(),
                e.getCliente().getNomeCliente(),
                Formatos.telefone(e.getCliente().getTelefone()),
                itens,
                e.isRetirada(),
                retirada ? "" : e.getEntrega().getEndereco(),
                e.getDestinatario(),
                Formatos.data(e.getDataEntrega()),
                e.getHoraEntrega(),
                e.getObservacao() == null ? "" : e.getObservacao(),
                e.getPagamento().getFormaPagamento(),
                retirada ? 0.0 : e.getEntrega().getFrete(),
                e.getValorEntrada(),
                e.getStatus(),
                Formatos.data(e.getCriadoEm()),
                e.getValorTotal());
    }

    public Encomenda paraDominio() {
        Encomenda e = new Encomenda();

        if (clienteId != null) {
            Clientes c = new Clientes();
            c.setId(clienteId);
            e.setCliente(c);
        }

        List<ItemEncomenda> lista = new ArrayList<>();
        if (itens != null) {
            for (ItemEncomendaDTO i : itens) {
                ItemEncomenda item = new ItemEncomenda(0,
                        i.qtd() == null ? 0 : i.qtd(),
                        i.preco() == null ? 0 : i.preco());
                if (i.produtoId() != null) {
                    Produto p = new Produto();
                    p.setId(i.produtoId());
                    item.setProduto(p);
                }
                lista.add(item);
            }
        }
        e.setItens(lista);

        boolean ehRetirada = Boolean.TRUE.equals(retirada);
        e.setRetirada(ehRetirada);
        e.setValorEntrada(entrada == null ? 0 : entrada);
        e.setPagamento(new Pagamentos(0, pagamento));
        e.setDestinatario(destinatario);
        e.setObservacao(observacao);
        e.setDataEntrega(Formatos.paraData(dataEntrega));
        e.setHoraEntrega(horaEntrega);
        if (!ehRetirada) {
            e.setEntrega(new Entrega(0, endereco, destinatario, frete == null ? 0 : frete, e.getDataEntrega()));
        }
        return e;
    }
}
