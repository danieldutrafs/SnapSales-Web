package br.com.snapsales.controllers;

import br.com.snapsales.services.RegraNegocioException;
import br.com.snapsales.models.Movimentacao;
import br.com.snapsales.models.MovimentacaoEstoque;
import br.com.snapsales.models.Produto;
import java.util.ArrayList;
import java.util.List;

/** Formato JSON de movimentacao manual (tipo = "Entrada" ou "Saída"). */
public record MovimentacaoDTO(Long id, String tipo, String data, String descricao,
                              List<ItemMovimentacaoDTO> itens) {

    private static final String SAIDA = "Sa\u00edda";

    public static MovimentacaoDTO de(Movimentacao m) {
        List<ItemMovimentacaoDTO> itens = new ArrayList<>();
        for (MovimentacaoEstoque l : m.getItens()) {
            itens.add(new ItemMovimentacaoDTO(l.getProduto().getId(), l.getProduto().getNomeProduto(),
                    l.getQuantidade(), l.getPrecoUnitario(),
                    Formatos.arredonda(l.getQuantidade() * l.getPrecoUnitario())));
        }
        return new MovimentacaoDTO(m.getId(), m.isEntrada() ? "Entrada" : SAIDA,
                Formatos.data(m.getData()), m.getDescricao() == null ? "" : m.getDescricao(), itens);
    }

    public Movimentacao paraDominio() {
        Movimentacao m = new Movimentacao();

        String t = tipo == null ? "" : tipo.trim().toLowerCase();
        if (t.equals("entrada")) {
            m.setEntrada(true);
        } else if (t.startsWith("sa")) {
            m.setEntrada(false);
        } else {
            throw new RegraNegocioException("Selecione o tipo.");
        }

        m.setData(Formatos.paraData(data));
        m.setDescricao(descricao == null ? "" : descricao.trim());

        List<MovimentacaoEstoque> linhas = new ArrayList<>();
        if (itens != null) {
            for (ItemMovimentacaoDTO i : itens) {
                MovimentacaoEstoque linha = new MovimentacaoEstoque();
                linha.setQuantidade(i.qtd() == null ? 0 : i.qtd());
                linha.setPrecoUnitario(i.preco() == null ? 0 : i.preco());
                if (i.produtoId() != null) {
                    Produto p = new Produto();
                    p.setId(i.produtoId());
                    linha.setProduto(p);
                }
                linhas.add(linha);
            }
        }
        m.setItens(linhas);
        return m;
    }
}
