package br.com.snapsales.controllers;

import br.com.snapsales.models.Clientes;

/** Formato JSON de cliente trocado com as telas. */
public record ClienteDTO(Long id, String codigo, String nome, String telefone, String endereco) {

    public static ClienteDTO de(Clientes c) {
        return new ClienteDTO(c.getId(), Formatos.codigo(c.getId()), c.getNomeCliente(),
                Formatos.telefone(c.getTelefone()), c.getEndereco() == null ? "" : c.getEndereco());
    }

    public Clientes paraDominio() {
        Clientes c = new Clientes();
        c.setNomeCliente(nome);
        c.setTelefone(telefone);
        c.setEndereco(endereco);
        return c;
    }
}
