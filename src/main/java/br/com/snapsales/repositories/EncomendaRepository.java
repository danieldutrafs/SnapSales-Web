package br.com.snapsales.repositories;

import br.com.snapsales.models.Encomenda;
import java.util.List;

public interface EncomendaRepository {

    /** Grava a encomenda e seus itens. Devolve o id gerado. */
    long salvar(Encomenda encomenda);

    /** Atualiza os dados e troca os itens. Nao altera status nem data de criacao. */
    boolean atualizar(long id, Encomenda encomenda);

    boolean atualizarStatus(long id, String status);

    boolean remover(long id);

    /** Da mais nova para a mais antiga. */
    List<Encomenda> listar();

    Encomenda buscarPorId(long id);
}
