package br.com.snapsales.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.snapsales.models.Movimentacao;
import br.com.snapsales.models.MovimentacaoEstoque;
import br.com.snapsales.models.Produto;
import br.com.snapsales.repositories.RepositoriosFalsos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Plano de teste - Classe MovimentacaoEstoque / MovimentacaoService (Testes 07 a 10). */
public class MovimentacaoServiceTest {

    private MovimentacaoService servico;
    private MovimentacaoEstoque movi;
    private Produto produto;

    @BeforeEach
    public void setUp() {
        RepositoriosFalsos.ProdutoFalso produtos = new RepositoriosFalsos.ProdutoFalso();
        servico = new MovimentacaoService(new RepositoriosFalsos.MovimentacaoFalsa(), produtos);

        // Produto para todos os testes (estoque inicial: 10)
        produto = new Produto();
        produto.setNomeProduto("Coxinha");
        produto.setPreco(3.00);
        produto.setQuantidade(10);
        produtos.salvar(produto);

        movi = new MovimentacaoEstoque();
        movi.setProduto(produto);
    }

    private Movimentacao movimentacaoDe(boolean entrada, int quantidade, String descricao) {
        MovimentacaoEstoque linha = new MovimentacaoEstoque();
        linha.setProduto(produto);
        linha.setQuantidade(quantidade);
        Movimentacao m = new Movimentacao();
        m.setEntrada(entrada);
        m.setData(LocalDate.now());
        m.setDescricao(descricao);
        List<MovimentacaoEstoque> itens = new ArrayList<>();
        itens.add(linha);
        m.setItens(itens);
        return m;
    }

    @Test
    @DisplayName("Teste 07 - Registra movimentação com quantidade positiva válida")
    public void deveAprovaMovimentacao() {
        movi.setQuantidade(50);
        movi.setTipoMovimentacao(true);

        boolean resultado = servico.salvar(movi);

        assertTrue(resultado, "A movimentação deveria ser salva pois os dados são válidos");
    }

    @Test
    @DisplayName("Teste 08 - Rejeita entrada com quantidade zero")
    public void deveBarrarMovimentacaoEntradaQuantidadeZero() {
        movi.setQuantidade(0);
        movi.setTipoMovimentacao(true);

        assertFalse(servico.salvar(movi), "A movimentação deve ser barrada pois a quantidade é zero");
    }

    @Test
    @DisplayName("Teste 08 - Rejeita saída com quantidade zero")
    public void deveBarrarMovimentacaoSaidaQuantidadeZero() {
        movi.setQuantidade(0);
        movi.setTipoMovimentacao(false);

        assertFalse(servico.salvar(movi), "A movimentação deve ser barrada pois a quantidade está zero");
    }

    @Test
    @DisplayName("Teste 09 - Rejeita entrada com quantidade negativa")
    public void deveBarrarMovimentacaoEntradaQuantidadeNegativa() {
        movi.setQuantidade(-25);
        movi.setTipoMovimentacao(true);

        assertFalse(servico.salvar(movi), "A movimentação deve ser barrada pois a quantidade está negativa");
    }

    @Test
    @DisplayName("Teste 09 - Rejeita saída com quantidade negativa")
    public void deveBarrarMovimentacaoSaidaQuantidadeNegativa() {
        movi.setQuantidade(-25);
        movi.setTipoMovimentacao(false);

        assertFalse(servico.salvar(movi), "A movimentação deve ser barrada pois a quantidade está negativa");
    }

    @Test
    @DisplayName("Teste 10 - Rejeita movimentação com produto nulo")
    public void deveBarrarMovimentacaoProdutoNulo() {
        movi.setProduto(null);
        movi.setQuantidade(10);
        movi.setTipoMovimentacao(true);

        assertFalse(servico.salvar(movi), "A movimentação deve ser barrada pois o produto está nulo");
    }

    @Test
    @DisplayName("Extra - Entrada soma no estoque do produto")
    public void entradaDeveSomarNoEstoque() {
        servico.criar(movimentacaoDe(true, 50, ""));

        assertEquals(60, produto.getQuantidade());
    }

    @Test
    @DisplayName("Extra - Saída subtrai do estoque do produto")
    public void saidaDeveSubtrairDoEstoque() {
        servico.criar(movimentacaoDe(false, 4, "Perda por validade"));

        assertEquals(6, produto.getQuantidade());
    }

    @Test
    @DisplayName("Extra - Saída maior que o estoque é rejeitada e não altera o estoque")
    public void deveBarrarSaidaMaiorQueEstoque() {
        assertThrows(RegraNegocioException.class,
                () -> servico.criar(movimentacaoDe(false, 11, "Perda por validade")));

        assertEquals(10, produto.getQuantidade());
    }

    @Test
    @DisplayName("Extra - Saída exige descrição")
    public void deveExigirDescricaoNaSaida() {
        assertThrows(RegraNegocioException.class, () -> servico.criar(movimentacaoDe(false, 1, "")));
    }

    @Test
    @DisplayName("Extra - Editar movimentação desfaz o efeito antigo e aplica o novo")
    public void editarDeveCorrigirEstoque() {
        Movimentacao criada = servico.criar(movimentacaoDe(true, 50, ""));   // estoque 60

        servico.atualizar(criada.getId(), movimentacaoDe(true, 20, ""));      // 10 + 20

        assertEquals(30, produto.getQuantidade());
    }

    @Test
    @DisplayName("Extra - Excluir movimentação devolve o estoque ao valor anterior")
    public void excluirDeveDesfazerEstoque() {
        Movimentacao criada = servico.criar(movimentacaoDe(false, 4, "Degustação"));   // estoque 6

        servico.excluir(criada.getId());

        assertEquals(10, produto.getQuantidade());
    }
}
