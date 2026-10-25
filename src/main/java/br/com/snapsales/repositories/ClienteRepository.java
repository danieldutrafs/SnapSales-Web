package br.com.snapsales.repositories;

import br.com.snapsales.models.Clientes;
import java.util.List;

/** Contrato de acesso aos dados de cliente. Quem implementa decide COMO guardar (JDBC, memoria...). */
public interface ClienteRepository {

    /** Grava um cliente novo e devolve o id gerado pelo banco. */
    long salvar(Clientes cliente);

    /** @return false se nao existe cliente com esse id */
    boolean atualizar(long id, Clientes cliente);

    /** @return false se nao existe cliente com esse id */
    boolean remover(long id);

    List<Clientes> listar();

    /** @return o cliente, ou null se nao existir */
    Clientes buscarPorId(long id);
}
