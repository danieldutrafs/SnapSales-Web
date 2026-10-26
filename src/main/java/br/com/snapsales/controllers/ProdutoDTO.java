package br.com.snapsales.controllers;

import br.com.snapsales.models.Categoria;
import br.com.snapsales.models.Produto;

public record ProdutoDTO(Long id, String codigo, String nome, CategoriaDTO categoria,
                         Integer quantidade, Double precoUni, Boolean ativo) {

    public static ProdutoDTO de(Produto p) {
        return new ProdutoDTO(p.getId(), Formatos.codigo(p.getId()), p.getNomeProduto(),
                CategoriaDTO.de(p.getCategoria()), p.getQuantidade(), p.getPreco(), p.isAtivo());
    }

    /** @param ativoPadrao valor usado quando a tela nao manda o campo "ativo" */
    public Produto paraDominio(boolean ativoPadrao) {
        Produto p = new Produto();
        p.setNomeProduto(nome);
        p.setQuantidade(quantidade == null ? -1 : quantidade);
        p.setPreco(precoUni == null ? -1 : precoUni);
        p.setAtivo(ativo == null ? ativoPadrao : ativo);
        if (categoria != null && categoria.id() != null) {
            p.setCategoria(new Categoria(categoria.id(), categoria.nome()));
        }
        return p;
    }
}
