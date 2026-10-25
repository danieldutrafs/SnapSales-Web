package br.com.snapsales.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.snapsales.models.Clientes;
import br.com.snapsales.repositories.RepositoriosFalsos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Plano de teste - Classe ClienteService (Testes 04, 05 e 06). */
public class ClienteServiceTest {

    private ClienteService servico;
    private Clientes cliente;

    @BeforeEach
    public void setUp() {
        servico = new ClienteService(new RepositoriosFalsos.ClienteFalso());
        cliente = new Clientes();
    }

    @Test
    @DisplayName("Teste 04 - Aprova cadastro com dados válidos")
    public void deveAprovarCliente() {
        cliente.setNomeCliente("Daniel");
        cliente.setTelefone("51999999999");

        boolean resultado = servico.salvar(cliente);

        assertTrue(resultado, "O cliente deveria ser salvo pois os dados são válidos");
        assertTrue(cliente.getId() > 0, "O id deve ser gerado ao salvar");
    }

    @Test
    @DisplayName("Teste 05 - Rejeita nome em branco")
    public void deveBarraNomeCliente() {
        cliente.setNomeCliente("");
        cliente.setTelefone("5188888888");

        assertFalse(servico.salvar(cliente), "O cliente deve ser rejeitado pois o nome está vazio");
    }

    @Test
    @DisplayName("Teste 05 - Rejeita nome nulo")
    public void deveBarraNomeNulo() {
        cliente.setNomeCliente(null);
        cliente.setTelefone("51999999999");

        assertFalse(servico.salvar(cliente), "O cliente deve ser rejeitado pois o nome está nulo");
    }

    @Test
    @DisplayName("Teste 06 - Rejeita telefone não preenchido")
    public void deveBarrarTelefoneVazio() {
        cliente.setNomeCliente("Arthur");
        cliente.setTelefone("");

        assertFalse(servico.salvar(cliente), "O cliente deve ser rejeitado pois o telefone está em branco");
    }

    @Test
    @DisplayName("Teste 06 - Rejeita telefone inválido (incompleto)")
    public void deveBarrarTelefoneIncompleto() {
        cliente.setNomeCliente("Eduardo");
        cliente.setTelefone("51999");

        assertFalse(servico.salvar(cliente), "O cliente deve ser rejeitado pois o telefone está incompleto");
    }

    @Test
    @DisplayName("Extra - Telefone com máscara é gravado só com dígitos")
    public void deveGravarTelefoneSoComDigitos() {
        cliente.setNomeCliente("Ana");
        cliente.setTelefone("51 999991111");
        cliente.setEndereco("Rua Central, 33");

        servico.criar(cliente);

        assertEquals("51999991111", cliente.getTelefone());
    }

    @Test
    @DisplayName("Extra - Editar cliente que não existe gera erro 'não encontrado'")
    public void deveFalharAoEditarClienteInexistente() {
        cliente.setNomeCliente("Ana");
        cliente.setTelefone("51999991111");

        assertThrows(NaoEncontradoException.class, () -> servico.atualizar(99L, cliente));
    }

    @Test
    @DisplayName("Extra - Excluir cliente que não existe gera erro 'não encontrado'")
    public void deveFalharAoExcluirClienteInexistente() {
        assertThrows(NaoEncontradoException.class, () -> servico.excluir(99L));
    }
}
