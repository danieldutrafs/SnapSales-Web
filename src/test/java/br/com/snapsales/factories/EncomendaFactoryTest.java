package br.com.snapsales.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import br.com.snapsales.models.Clientes;
import br.com.snapsales.models.Encomenda;
import br.com.snapsales.models.Entrega;
import br.com.snapsales.models.ItemEncomenda;
import br.com.snapsales.models.Pagamentos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Plano de teste - Classe EncomendaFactory (Testes 01, 02 e 03). */
public class EncomendaFactoryTest {

    private Clientes cliente;
    private Pagamentos pagamento;
    private List<ItemEncomenda> itens;

    @BeforeEach
    public void setUp() {
        cliente = new Clientes(1L, "Maria", "51999999999");
        pagamento = new Pagamentos(1L, "Pix");
        itens = new ArrayList<>();
        itens.add(new ItemEncomenda(1L, 2, 10.0));   // 20,00
        itens.add(new ItemEncomenda(2L, 3, 5.50));   // 16,50
    }

    @Test
    @DisplayName("Teste 01 - Valor total com múltiplos itens")
    public void deveCalcularValorTotalComMultiplosItens() {
        Encomenda e = EncomendaFactory.criarEncomenda(0, cliente, "preparacao", 0, true, itens, pagamento, null);

        assertEquals(36.50, e.getValorTotal(), 0.001);
    }

    @Test
    @DisplayName("Teste 01 - Itens ficam vinculados à encomenda criada")
    public void deveVincularItensNaEncomenda() {
        Encomenda e = EncomendaFactory.criarEncomenda(0, cliente, "preparacao", 0, true, itens, pagamento, null);

        assertSame(e, itens.get(0).getEncomenda());
        assertSame(e, itens.get(1).getEncomenda());
    }

    @Test
    @DisplayName("Teste 02 - Lista de itens vazia resulta em total zero")
    public void deveRetornarTotalZeroComListaVazia() {
        Encomenda e = EncomendaFactory.criarEncomenda(0, cliente, "preparacao", 0, true,
                new ArrayList<>(), pagamento, null);

        assertEquals(0.0, e.getValorTotal(), 0.001);
    }

    @Test
    @DisplayName("Teste 02 - Lista de itens nula resulta em total zero (sem exceção)")
    public void deveRetornarTotalZeroComListaNula() {
        Encomenda e = EncomendaFactory.criarEncomenda(0, cliente, "preparacao", 0, true, null, pagamento, null);

        assertEquals(0.0, e.getValorTotal(), 0.001);
    }

    @Test
    @DisplayName("Teste 03 - Entrega soma a taxa de frete no valor final")
    public void deveSomarFreteQuandoForEntrega() {
        Entrega entrega = new Entrega(1L, "Rua A, 10", "Maria", 10.0, LocalDate.now());

        Encomenda e = EncomendaFactory.criarEncomenda(0, cliente, "preparacao", 0, false, itens, pagamento, entrega);

        assertEquals(46.50, e.getValorTotal(), 0.001);   // 36,50 + 10,00
    }

    @Test
    @DisplayName("Teste 03 - Retirada no local não cobra frete")
    public void deveIgnorarFreteQuandoForRetirada() {
        Entrega entrega = new Entrega(1L, "Rua A, 10", "Maria", 10.0, LocalDate.now());

        Encomenda e = EncomendaFactory.criarEncomenda(0, cliente, "preparacao", 0, true, itens, pagamento, entrega);

        assertEquals(36.50, e.getValorTotal(), 0.001);
    }
}
