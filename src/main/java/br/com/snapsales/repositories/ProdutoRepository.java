package br.com.snapsales.repositories;

import br.com.snapsales.models.Produto;
import java.util.List;

public interface ProdutoRepository {

    long salvar(Produto produto);

    /** Atualiza nome, quantidade, preco e categoria (nao mexe no campo ativo). */
    boolean atualizar(long id, Produto produto);

    boolean atualizarAtivo(long id, boolean ativo);

    boolean atualizarQuantidade(long id, int quantidade);

    boolean remover(long id);

    List<Produto> listar();

    Produto buscarPorId(long id);
}
