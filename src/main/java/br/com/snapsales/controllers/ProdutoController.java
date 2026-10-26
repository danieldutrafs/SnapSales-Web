package br.com.snapsales.controllers;

import br.com.snapsales.services.ProdutoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProdutoDTO> listar() {
        return service.listar().stream().map(ProdutoDTO::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoDTO criar(@RequestBody ProdutoDTO dto) {
        return ProdutoDTO.de(service.criar(dto.paraDominio(true)));
    }

    @PutMapping("/{id}")
    public ProdutoDTO atualizar(@PathVariable("id") long id, @RequestBody ProdutoDTO dto) {
        boolean ativoAtual = service.buscar(id).isAtivo();
        return ProdutoDTO.de(service.atualizar(id, dto.paraDominio(ativoAtual)));
    }

    /** Pausa ou reativa o produto. */
    @PatchMapping("/{id}/ativo")
    public ProdutoDTO alternarAtivo(@PathVariable("id") long id) {
        return ProdutoDTO.de(service.alternarAtivo(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable("id") long id) {
        service.excluir(id);
    }
}
