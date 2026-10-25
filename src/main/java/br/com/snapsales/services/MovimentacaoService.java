package br.com.snapsales.services;

import br.com.snapsales.models.Movimentacao;
import br.com.snapsales.models.MovimentacaoEstoque;
import br.com.snapsales.models.Produto;
import br.com.snapsales.repositories.MovimentacaoRepository;
import br.com.snapsales.repositories.ProdutoRepository;
import br.com.snapsales.utils.ValidadorUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Movimentacoes manuais de estoque. Toda entrada soma no estoque do produto e toda saida subtrai.
 * Ao editar ou excluir, o efeito antigo e desfeito antes de aplicar o novo.
 *
 * @author Daniel Dutra
 */
@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository, ProdutoRepository produtoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<Movimentacao> listar() {
        return movimentacaoRepository.listar();
    }

    public Movimentacao buscar(long id) {
        Movimentacao movimentacao = movimentacaoRepository.buscarPorId(id);
        if (movimentacao == null) {
            throw new NaoEncontradoException("Movimentação não encontrada.");
        }
        return movimentacao;
    }

    @Transactional
    public Movimentacao criar(Movimentacao movimentacao) {
        validar(movimentacao);
        aplicarNoEstoque(movimentacao, false);
        movimentacao.setId(movimentacaoRepository.salvar(movimentacao));
        return movimentacao;
    }

    @Transactional
    public Movimentacao atualizar(long id, Movimentacao movimentacao) {
        Movimentacao antiga = buscar(id);
        validar(movimentacao);
        aplicarNoEstoque(antiga, true);       // desfaz o efeito antigo
        aplicarNoEstoque(movimentacao, false); // aplica o novo
        if (!movimentacaoRepository.atualizar(id, movimentacao)) {
            throw new NaoEncontradoException("Movimentação não encontrada.");
        }
        movimentacao.setId(id);
        return movimentacao;
    }

    @Transactional
    public void excluir(long id) {
        Movimentacao antiga = buscar(id);
        aplicarNoEstoque(antiga, true);
        movimentacaoRepository.remover(id);
    }

    /** Versao simples (uma linha so) usada nos testes: true se registrou, false se invalida. */
    @Transactional
    public boolean salvar(MovimentacaoEstoque movi) {
        try {
            Movimentacao movimentacao = new Movimentacao();
            movimentacao.setEntrada(movi.isTipoMovimentacao());
            movimentacao.setData(LocalDate.now());
            movimentacao.setDescricao("Movimentação avulsa");
            List<MovimentacaoEstoque> linhas = new ArrayList<>();
            linhas.add(movi);
            movimentacao.setItens(linhas);
            criar(movimentacao);
            System.out.println("Movimentação registrada com sucesso! ID: " + movimentacao.getId());
            return true;
        } catch (RegraNegocioException e) {
            System.out.println("Erro: " + e.getMessage());
            return false;
        }
    }

    /* ---------------------------------------------------------------- */

    private void validar(Movimentacao m) {
        if (m == null || !ValidadorUtil.validarLista(m.getItens())) {
            throw new RegraNegocioException("Adicione pelo menos um item.");
        }
        for (MovimentacaoEstoque linha : m.getItens()) {
            if (linha == null || linha.getProduto() == null) {
                throw new RegraNegocioException("A movimentação precisa estar associada a um produto.");
            }
            if (!ValidadorUtil.validarCampoInt(linha.getQuantidade())) {
                throw new RegraNegocioException("A quantidade da movimentação deve ser maior que zero.");
            }
        }
        if (m.getData() == null) {
            throw new RegraNegocioException("Informe a data.");
        }
        if (!m.isEntrada() && (m.getDescricao() == null || m.getDescricao().trim().length() < 3)) {
            throw new RegraNegocioException("Descreva o motivo da saída.");
        }
    }

    /**
     * Soma ou subtrai as quantidades no estoque dos produtos.
     *
     * @param desfazer true = faz o contrario (usado ao editar ou excluir)
     */
    private void aplicarNoEstoque(Movimentacao m, boolean desfazer) {
        boolean somar = m.isEntrada() != desfazer;
        for (MovimentacaoEstoque linha : m.getItens()) {
            Produto produto = produtoRepository.buscarPorId(linha.getProduto().getId());
            if (produto == null) {
                throw new RegraNegocioException("Produto não encontrado.");
            }
            int novaQuantidade = produto.getQuantidade() + (somar ? linha.getQuantidade() : -linha.getQuantidade());
            if (novaQuantidade < 0) {
                throw new RegraNegocioException("Estoque insuficiente de " + produto.getNomeProduto()
                        + " (disponível: " + produto.getQuantidade() + " un.).");
            }
            produtoRepository.atualizarQuantidade(produto.getId(), novaQuantidade);
            linha.setTipoMovimentacao(m.isEntrada());
            if (linha.getPrecoUnitario() <= 0) {
                linha.setPrecoUnitario(produto.getPreco());
            }
        }
    }
}
