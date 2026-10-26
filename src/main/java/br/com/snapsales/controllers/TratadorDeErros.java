package br.com.snapsales.controllers;

import br.com.snapsales.services.NaoEncontradoException;
import br.com.snapsales.services.RegraNegocioException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Transforma erros em respostas JSON {"erro": "mensagem"} que as telas mostram para a pessoa. */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> regraDeNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(NaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridade(DataIntegrityViolationException e) {
        return resposta(HttpStatus.CONFLICT,
                "Operação não permitida: este registro está vinculado a outros dados "
                + "(ex.: encomendas ou movimentações) ou já existe.");
    }

    @ExceptionHandler({DateTimeParseException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> formatoInvalido(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "Dados enviados em formato inválido (confira datas e números).");
    }

    private ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
