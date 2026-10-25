package br.com.snapsales.services;

import br.com.snapsales.models.Categoria;
import br.com.snapsales.models.Produto;
import br.com.snapsales.repositories.CategoriaRepository;
import br.com.snapsales.repositories.ProdutoRepository;
import br.com.snapsales.utils.ValidadorUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author Daniel Dutra
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Produto> listar() {
        return produtoRepository.listar();
    }

    public Produto buscar(long id) {
        Produto produto = produtoRepository.buscarPorId(id);
        if (produto == null) {
            throw new NaoEncontradoException("Produto não encontrado.");
        }
        return produto;
    }

    public Produto criar(Produto produto) {
        validar(produto, 0);
        produto.setId(produtoRepository.salvar(produto));
        return produto;
    }

    /** Atualiza nome, quantidade, preco e categoria. O campo "ativo" nao muda aqui. */
    public Produto atualizar(long id, Produto produto) {
        buscar(id);
        validar(produto, id);
        if (!produtoRepository.atualizar(id, produto)) {
            throw new NaoEncontradoException("Produto não encontrado.");
        }
        produto.setId(id);
        return produto;
    }

    public Produto alternarAtivo(long id) {
        Produto produto = buscar(id);
        produto.setAtivo(!produto.isAtivo());
        produtoRepository.atualizarAtivo(id, produto.isAtivo());
        return produto;
    }

    public void excluir(long id) {
        if (!produtoRepository.remover(id)) {
            throw new NaoEncontradoException("Produto não encontrado.");
        }
    }

    /** Versao simples: true se salvou, false se os dados eram invalidos. */
    public boolean salvar(Produto produto) {
        try {
            criar(produto);
            System.out.println("Produto cadastrado junto a categoria!");
            return true;
        } catch (RegraNegocioException e) {
            System.out.println("Erro: " + e.getMessage());
            return false;
        }
    }

    private void validar(Produto produto, long idAtual) {
        if (produto == null || !ValidadorUtil.validarCampoTexto(produto.getNomeProduto())) {
            throw new RegraNegocioException("Informe o nome do produto.");
        }
        // A tela permite estoque 0 e preco 0
        if (!ValidadorUtil.validarQuantidadeNaoNegativa(produto.getQuantidade())
                || !ValidadorUtil.validarValorNaoNegativo(produto.getPreco())) {
            throw new RegraNegocioException("Dados do produto inválidos (quantidade e preço não podem ser negativos).");
        }
        if (produto.getCategoria() == null) {
            throw new RegraNegocioException("O produto precisa estar associado a uma categoria!");
        }
        Categoria categoria = categoriaRepository.buscarPorId(produto.getCategoria().getId());
        if (categoria == null) {
            throw new RegraNegocioException("Categoria não encontrada.");
        }
        produto.setCategoria(categoria);

        String nome = produto.getNomeProduto().trim();
        produto.setNomeProduto(nome);
        for (Produto outro : produtoRepository.listar()) {
            if (outro.getId() != idAtual && outro.getNomeProduto().equalsIgnoreCase(nome)) {
                throw new RegraNegocioException("Já existe um produto com esse nome.");
            }
        }
    }
}
