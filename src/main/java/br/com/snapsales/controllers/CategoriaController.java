package br.com.snapsales.controllers;

import br.com.snapsales.services.CategoriaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaDTO> listar() {
        return service.listar().stream().map(CategoriaDTO::de).toList();
    }

    /** Devolve a categoria com esse nome; se nao existir, cria. */
    @PostMapping
    public CategoriaDTO obterOuCriar(@RequestBody CategoriaDTO dto) {
        return CategoriaDTO.de(service.obterOuCriar(dto.nome()));
    }
}
