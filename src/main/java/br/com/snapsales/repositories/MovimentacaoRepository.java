package br.com.snapsales.repositories;

import br.com.snapsales.models.Movimentacao;
import java.util.List;

public interface MovimentacaoRepository {

    /** Grava a movimentacao e suas linhas. Devolve o id gerado. */
    long salvar(Movimentacao movimentacao);

    /** Atualiza o cabecalho e troca as linhas. */
    boolean atualizar(long id, Movimentacao movimentacao);

    boolean remover(long id);

    List<Movimentacao> listar();

    Movimentacao buscarPorId(long id);
}
