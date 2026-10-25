package br.com.snapsales.services;

/** Registro nao existe (vira resposta 404 para a tela). */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
