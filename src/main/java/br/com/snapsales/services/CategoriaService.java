package br.com.snapsales.services;

import br.com.snapsales.models.Categoria;
import br.com.snapsales.repositories.CategoriaRepository;
import br.com.snapsales.utils.ValidadorUtil;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.listar();
    }

    /** Devolve a categoria com esse nome; se nao existir, cria. */
    public Categoria obterOuCriar(String nome) {
        if (!ValidadorUtil.validarCampoTexto(nome)) {
            throw new RegraNegocioException("Informe o nome da categoria.");
        }
        String nomeLimpo = nome.trim();
        Categoria existente = categoriaRepository.buscarPorNome(nomeLimpo);
        if (existente != null) {
            return existente;
        }
        Categoria nova = new Categoria(0, nomeLimpo);
        nova.setId(categoriaRepository.salvar(nova));
        return nova;
    }
}
