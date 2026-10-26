package br.com.snapsales.controllers;

import br.com.snapsales.services.EncomendaService;
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
@RequestMapping("/api/encomendas")
public class EncomendaController {

    private final EncomendaService service;

    public EncomendaController(EncomendaService service) {
        this.service = service;
    }

    @GetMapping
    public List<EncomendaDTO> listar() {
        return service.listar().stream().map(EncomendaDTO::de).toList();
    }

    @GetMapping("/{id}")
    public EncomendaDTO buscar(@PathVariable("id") long id) {
        return EncomendaDTO.de(service.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EncomendaDTO criar(@RequestBody EncomendaDTO dto) {
        return EncomendaDTO.de(service.criar(dto.paraDominio()));
    }

    @PutMapping("/{id}")
    public EncomendaDTO atualizar(@PathVariable("id") long id, @RequestBody EncomendaDTO dto) {
        return EncomendaDTO.de(service.atualizar(id, dto.paraDominio()));
    }

    /** Muda o status: preparacao -> pronto -> entregue. */
    @PatchMapping("/{id}/status")
    public EncomendaDTO alterarStatus(@PathVariable("id") long id, @RequestBody StatusDTO dto) {
        return EncomendaDTO.de(service.alterarStatus(id, dto.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable("id") long id) {
        service.excluir(id);
    }
}
