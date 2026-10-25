package br.com.snapsales.services;

import br.com.snapsales.models.Clientes;
import br.com.snapsales.repositories.ClienteRepository;
import br.com.snapsales.utils.ValidadorUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Regras de cliente. Depende apenas da interface ClienteRepository (SOLID: inversao de dependencia).
 *
 * @author Daniel Dutra
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Clientes> listar() {
        return clienteRepository.listar();
    }

    public Clientes criar(Clientes cliente) {
        validarEPadronizar(cliente);
        cliente.setId(clienteRepository.salvar(cliente));
        return cliente;
    }

    public Clientes atualizar(long id, Clientes cliente) {
        validarEPadronizar(cliente);
        if (!clienteRepository.atualizar(id, cliente)) {
            throw new NaoEncontradoException("Cliente não encontrado.");
        }
        cliente.setId(id);
        return cliente;
    }

    public void excluir(long id) {
        if (!clienteRepository.remover(id)) {
            throw new NaoEncontradoException("Cliente não encontrado.");
        }
    }

    /** Versao simples usada nos testes: true se salvou, false se os dados eram invalidos. */
    public boolean salvar(Clientes cliente) {
        try {
            criar(cliente);
            System.out.println("Cliente cadastrado com sucesso! ID: " + cliente.getId());
            return true;
        } catch (RegraNegocioException e) {
            System.out.println("Erro: " + e.getMessage());
            return false;
        }
    }

    private void validarEPadronizar(Clientes cliente) {
        if (cliente == null || !ValidadorUtil.validarCampoTexto(cliente.getNomeCliente())) {
            throw new RegraNegocioException("O nome do cliente não pode estar vazio.");
        }
        String telefoneLimpo = limparTelefone(cliente.getTelefone());
        if (telefoneLimpo.length() != 10 && telefoneLimpo.length() != 11) {
            throw new RegraNegocioException("Telefone inválido. Deve conter 10 ou 11 dígitos numéricos.");
        }
        cliente.setNomeCliente(cliente.getNomeCliente().trim());
        cliente.setTelefone(telefoneLimpo);
        cliente.setEndereco(cliente.getEndereco() == null ? null : cliente.getEndereco().trim());
    }

    private String limparTelefone(String telefone) {
        if (telefone == null) {
            return "";
        }
        return telefone.replaceAll("\\D", "");
    }
}
