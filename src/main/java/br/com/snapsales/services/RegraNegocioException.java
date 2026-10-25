package br.com.snapsales.services;

/** Erro de validacao ou regra de negocio (vira resposta 400 para a tela). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
