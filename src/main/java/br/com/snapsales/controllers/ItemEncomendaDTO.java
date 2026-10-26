package br.com.snapsales.controllers;

public record ItemEncomendaDTO(Long produtoId, String produto, Integer qtd, Double preco) {
}
