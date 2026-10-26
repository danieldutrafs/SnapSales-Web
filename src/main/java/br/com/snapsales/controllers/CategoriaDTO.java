package br.com.snapsales.controllers;

import br.com.snapsales.models.Categoria;

public record CategoriaDTO(Long id, String nome) {

    public static CategoriaDTO de(Categoria c) {
        return new CategoriaDTO(c.getId(), c.getNomeCategoria());
    }
}
