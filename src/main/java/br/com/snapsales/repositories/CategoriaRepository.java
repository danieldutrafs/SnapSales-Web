package br.com.snapsales.repositories;

import br.com.snapsales.models.Categoria;
import java.util.List;

public interface CategoriaRepository {

    long salvar(Categoria categoria);

    List<Categoria> listar();

    /** @return a categoria, ou null se nao existir */
    Categoria buscarPorId(long id);

    /** Busca ignorando maiusculas/minusculas. @return a categoria, ou null se nao existir */
    Categoria buscarPorNome(String nome);
}
