package br.com.snapsales.controllers;

import br.com.snapsales.services.MovimentacaoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService service;

    public MovimentacaoController(MovimentacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MovimentacaoDTO> listar() {
        return service.listar().stream().map(MovimentacaoDTO::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoDTO criar(@RequestBody MovimentacaoDTO dto) {
        return MovimentacaoDTO.de(service.criar(dto.paraDominio()));
    }

    @PutMapping("/{id}")
    public MovimentacaoDTO atualizar(@PathVariable("id") long id, @RequestBody MovimentacaoDTO dto) {
        return MovimentacaoDTO.de(service.atualizar(id, dto.paraDominio()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable("id") long id) {
        service.excluir(id);
    }
}
