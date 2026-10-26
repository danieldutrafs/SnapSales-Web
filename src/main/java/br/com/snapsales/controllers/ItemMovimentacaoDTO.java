package br.com.snapsales.controllers;

public record ItemMovimentacaoDTO(Long produtoId, String produto, Integer qtd, Double preco, Double total) {
}
