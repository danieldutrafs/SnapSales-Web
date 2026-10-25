package br.com.snapsales.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.snapsales.models.Clientes;
import br.com.snapsales.models.Encomenda;
import br.com.snapsales.models.Entrega;
import br.com.snapsales.models.ItemEncomenda;
import br.com.snapsales.models.Pagamentos;
import br.com.snapsales.models.Produto;
import br.com.snapsales.repositories.RepositoriosFalsos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Regras de cadastro de encomenda (validações, total com frete e estoque). */
public class EncomendaServiceTest {

    private EncomendaService servico;
    private Encomenda encomenda;
    private Clientes cliente;
    private Produto produto;
    private List<ItemEncomenda> itens;

    @BeforeEach
    public void setUp() {
        RepositoriosFalsos.ClienteFalso clientes = new RepositoriosFalsos.ClienteFalso();
        RepositoriosFalsos.ProdutoFalso produtos = new RepositoriosFalsos.ProdutoFalso();
        servico = new EncomendaService(new RepositoriosFalsos.EncomendaFalsa(), clientes, produtos);

        cliente = new Clientes(0, "Maria", "51999999999");
        cliente.setId(clientes.salvar(cliente));

        produto = new Produto();
        produto.setNomeProduto("Coxinha");
        produto.setPreco(10.0);
        produto.setQuantidade(100);
        produtos.salvar(produto);

        ItemEncomenda item = new ItemEncomenda(0, 2, 10.0);
        item.setProduto(produto);
        itens = new ArrayList<>();
        itens.add(item);

        encomenda = new Encomenda();
        encomenda.setCliente(cliente);
        encomenda.setPagamento(new Pagamentos(1L, "Dinheiro"));
        encomenda.setItens(itens);
        encomenda.setValorEntrada(5.0);
        encomenda.setRetirada(true);
        encomenda.setDestinatario("Maria");
        encomenda.setDataEntrega(LocalDate.now());
        encomenda.setHoraEntrega("12:00");
    }

    @Test
    public void deveSalvarEncomendaComSucesso() {
        assertTrue(servico.salvar(encomenda));
    }

    @Test
    public void deveBarraEncomendaSemCliente() {
        encomenda.setCliente(null);
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    public void deveBarraValorEntradaNegativo() {
        encomenda.setValorEntrada(-10.0);
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    public void deveBarraEncomendaSemItens() {
        encomenda.setItens(new ArrayList<>());
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    public void deveBarraItemComQuantidadeOuPrecoInvalido() {
        itens.get(0).setQuantidadeItem(0);
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    public void deveBarraPagamentoNuloOuInvalido() {
        encomenda.setPagamento(null);
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    public void deveBarraEntregaIncompletaQuandoNaoForRetirada() {
        encomenda.setRetirada(false);
        encomenda.setEntrega(null);
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    public void deveSalvarEntregaCompletaComSucesso() {
        encomenda.setRetirada(false);
        encomenda.setEntrega(new Entrega(1L, "Rua A", "Destinatario", 10.0, LocalDate.now()));
        assertTrue(servico.salvar(encomenda));
    }

    @Test
    public void deveBarraValorEntradaMaiorQueTotal() {
        encomenda.setValorEntrada(50.0);
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    @DisplayName("Extra - Total salvo inclui o frete quando é entrega")
    public void totalDaEncomendaDeveIncluirFrete() {
        encomenda.setRetirada(false);
        encomenda.setEntrega(new Entrega(1L, "Rua A", "Destinatario", 10.0, LocalDate.now()));

        Encomenda salva = servico.criar(encomenda);

        assertEquals(30.0, salva.getValorTotal(), 0.001);   // 2 x 10,00 + 10,00 de frete
    }

    @Test
    @DisplayName("Extra - Rejeita item com quantidade maior que o estoque")
    public void deveBarrarItemMaiorQueEstoque() {
        itens.get(0).setQuantidadeItem(101);
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    @DisplayName("Extra - Rejeita data de entrega anterior a hoje")
    public void deveBarrarDataDeEntregaNoPassado() {
        encomenda.setDataEntrega(LocalDate.now().minusDays(1));
        assertFalse(servico.salvar(encomenda));
    }

    @Test
    @DisplayName("Extra - Nova encomenda começa 'em preparação'")
    public void novaEncomendaDeveComecarEmPreparacao() {
        assertEquals(Encomenda.PREPARACAO, servico.criar(encomenda).getStatus());
    }

    @Test
    @DisplayName("Extra - Criar a encomenda já baixa o estoque")
    public void criarDeveBaixarEstoque() {
        servico.criar(encomenda);

        assertEquals(98, produto.getQuantidade());   // 100 - 2
    }

    @Test
    @DisplayName("Extra - Entregar a encomenda não baixa o estoque de novo")
    public void entregarNaoDeveBaixarEstoqueDeNovo() {
        Encomenda salva = servico.criar(encomenda);

        servico.alterarStatus(salva.getId(), Encomenda.PRONTO);
        servico.alterarStatus(salva.getId(), Encomenda.ENTREGUE);

        assertEquals(98, produto.getQuantidade());
    }

    @Test
    @DisplayName("Extra - Editar a encomenda ajusta o estoque")
    public void editarDeveAjustarEstoque() {
        Encomenda salva = servico.criar(encomenda);   // estoque 98
        itens.get(0).setQuantidadeItem(5);

        servico.atualizar(salva.getId(), encomenda);

        assertEquals(95, produto.getQuantidade());    // devolveu 2 e baixou 5
    }

    @Test
    @DisplayName("Extra - Excluir encomenda não entregue devolve o estoque")
    public void excluirDeveDevolverEstoque() {
        Encomenda salva = servico.criar(encomenda);

        servico.excluir(salva.getId());

        assertEquals(100, produto.getQuantidade());
    }

    @Test
    @DisplayName("Extra - Excluir encomenda entregue não devolve o estoque")
    public void excluirEntregueNaoDeveDevolverEstoque() {
        Encomenda salva = servico.criar(encomenda);
        servico.alterarStatus(salva.getId(), Encomenda.ENTREGUE);

        servico.excluir(salva.getId());

        assertEquals(98, produto.getQuantidade());
    }

    @Test
    @DisplayName("Extra - Não é possível voltar o status da encomenda")
    public void naoDeveVoltarStatus() {
        Encomenda salva = servico.criar(encomenda);
        servico.alterarStatus(salva.getId(), Encomenda.PRONTO);

        assertThrows(RegraNegocioException.class,
                () -> servico.alterarStatus(salva.getId(), Encomenda.PREPARACAO));
    }

    @Test
    @DisplayName("Extra - Encomenda entregue não pode ser editada")
    public void encomendaEntregueNaoPodeSerEditada() {
        Encomenda salva = servico.criar(encomenda);
        servico.alterarStatus(salva.getId(), Encomenda.ENTREGUE);

        assertThrows(RegraNegocioException.class, () -> servico.atualizar(salva.getId(), encomenda));
    }
}
