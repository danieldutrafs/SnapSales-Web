package br.com.snapsales.services;

import br.com.snapsales.factories.EncomendaFactory;
import br.com.snapsales.models.Clientes;
import br.com.snapsales.models.Encomenda;
import br.com.snapsales.models.ItemEncomenda;
import br.com.snapsales.models.Produto;
import br.com.snapsales.repositories.ClienteRepository;
import br.com.snapsales.repositories.EncomendaRepository;
import br.com.snapsales.repositories.ProdutoRepository;
import br.com.snapsales.utils.ValidadorUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de encomenda.
 *
 * - Valida os dados e o estoque disponivel de cada item.
 * - O estoque e baixado assim que a encomenda e CRIADA (reservado para ela).
 * - Ao editar, o estoque antigo e devolvido e o novo e baixado. Ao excluir uma encomenda que ainda
 *   nao foi entregue, o estoque volta.
 *
 * @author Daniel Dutra
 */
@Service
public class EncomendaService {

    private static final List<String> ORDEM_STATUS =
            List.of(Encomenda.PREPARACAO, Encomenda.PRONTO, Encomenda.ENTREGUE);

    private final EncomendaRepository encomendaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public EncomendaService(EncomendaRepository encomendaRepository,
                            ClienteRepository clienteRepository,
                            ProdutoRepository produtoRepository) {
        this.encomendaRepository = encomendaRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<Encomenda> listar() {
        return encomendaRepository.listar();
    }

    public Encomenda buscar(long id) {
        Encomenda encomenda = encomendaRepository.buscarPorId(id);
        if (encomenda == null) {
            throw new NaoEncontradoException("Encomenda não encontrada.");
        }
        return encomenda;
    }

    @Transactional
    public Encomenda criar(Encomenda dados) {
        validar(dados, true);
        Encomenda nova = montar(dados);
        nova.setStatus(Encomenda.PREPARACAO);
        nova.setCriadoEm(LocalDate.now());
        nova.setId(encomendaRepository.salvar(nova));
        baixarEstoque(nova);
        return nova;
    }

    @Transactional
    public Encomenda atualizar(long id, Encomenda dados) {
        Encomenda atual = buscar(id);
        if (Encomenda.ENTREGUE.equals(atual.getStatus())) {
            throw new RegraNegocioException("Encomendas entregues não podem ser editadas.");
        }
        validar(dados, false);
        devolverEstoque(atual);                 // devolve o que a encomenda antiga tinha reservado
        Encomenda editada = montar(dados);      // confere o estoque ja com a devolucao
        editada.setStatus(atual.getStatus());
        editada.setCriadoEm(atual.getCriadoEm());
        if (!encomendaRepository.atualizar(id, editada)) {
            throw new NaoEncontradoException("Encomenda não encontrada.");
        }
        editada.setId(id);
        baixarEstoque(editada);
        return editada;
    }

    /** preparacao -> pronto -> entregue (o estoque ja foi baixado na criacao). */
    @Transactional
    public Encomenda alterarStatus(long id, String novoStatus) {
        int novo = ORDEM_STATUS.indexOf(novoStatus);
        if (novo < 0) {
            throw new RegraNegocioException("Status inválido.");
        }
        Encomenda atual = buscar(id);
        int antigo = ORDEM_STATUS.indexOf(atual.getStatus());
        if (novo < antigo) {
            throw new RegraNegocioException("Não é possível voltar o status da encomenda.");
        }
        if (novo == antigo) {
            return atual;
        }
        encomendaRepository.atualizarStatus(id, novoStatus);
        atual.setStatus(novoStatus);
        return atual;
    }

    @Transactional
    public void excluir(long id) {
        Encomenda encomenda = buscar(id);
        if (!Encomenda.ENTREGUE.equals(encomenda.getStatus())) {
            devolverEstoque(encomenda);   // encomenda entregue ja saiu da loja: nao devolve
        }
        encomendaRepository.remover(id);
    }

    /** Versao simples usada nos testes: true se salvou, false se os dados eram invalidos. */
    public boolean salvar(Encomenda encomenda) {
        try {
            Encomenda salva = criar(encomenda);
            System.out.println("Encomenda cadastrada com sucesso! ID: " + salva.getId());
            return true;
        } catch (RegraNegocioException e) {
            System.out.println("Erro: " + e.getMessage());
            return false;
        }
    }

    /* ---------------------------------------------------------------- */

    private void validar(Encomenda e, boolean criacao) {
        if (e == null || e.getCliente() == null) {
            throw new RegraNegocioException("A encomenda precisa estar vinculada a um cliente.");
        }
        if (e.getValorEntrada() < 0) {
            throw new RegraNegocioException("O valor de entrada não pode ser negativo.");
        }
        if (!ValidadorUtil.validarLista(e.getItens())) {
            throw new RegraNegocioException("A encomenda deve conter pelo menos um item.");
        }
        for (ItemEncomenda item : e.getItens()) {
            if (item == null
                    || !ValidadorUtil.validarCampoInt(item.getQuantidadeItem())
                    || !ValidadorUtil.validarCampoDouble(item.getPrecoMomento())) {
                throw new RegraNegocioException("Há itens na encomenda com quantidade ou preço inválidos.");
            }
            if (item.getProduto() == null) {
                throw new RegraNegocioException("Há itens na encomenda sem produto.");
            }
        }
        if (e.getPagamento() == null || !ValidadorUtil.validarCampoTexto(e.getPagamento().getFormaPagamento())) {
            throw new RegraNegocioException("A forma de pagamento é obrigatória.");
        }
        if (!ValidadorUtil.validarCampoTexto(e.getDestinatario())) {
            throw new RegraNegocioException("Informe o nome do destinatário.");
        }
        if (e.getDataEntrega() == null || !ValidadorUtil.validarCampoTexto(e.getHoraEntrega())) {
            throw new RegraNegocioException("Informe a data e a hora da entrega.");
        }
        if (criacao && e.getDataEntrega().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data da entrega não pode ser anterior a hoje.");
        }
        if (!e.isRetirada()) {
            if (e.getEntrega() == null
                    || !ValidadorUtil.validarCampoTexto(e.getEntrega().getEndereco())
                    || !ValidadorUtil.validarCampoTexto(e.getEntrega().getDestinatario())) {
                throw new RegraNegocioException("Endereço e destinatário são obrigatórios para encomendas do tipo entrega.");
            }
            if (e.getEntrega().getFrete() < 0) {
                throw new RegraNegocioException("O valor do frete não pode ser negativo.");
            }
            if (e.getEntrega().getDataEntrega() == null) {
                throw new RegraNegocioException("A data de entrega é obrigatória.");
            }
        }
    }

    /** Confere cliente, produtos e estoque no banco e monta a encomenda com o total calculado. */
    private Encomenda montar(Encomenda dados) {
        Clientes cliente = clienteRepository.buscarPorId(dados.getCliente().getId());
        if (cliente == null) {
            throw new RegraNegocioException("Cliente não encontrado.");
        }

        List<ItemEncomenda> itens = new ArrayList<>();
        for (ItemEncomenda item : dados.getItens()) {
            Produto produto = produtoRepository.buscarPorId(item.getProduto().getId());
            if (produto == null) {
                throw new RegraNegocioException("Produto não encontrado.");
            }
            if (item.getQuantidadeItem() > produto.getQuantidade()) {
                throw new RegraNegocioException("Estoque insuficiente de " + produto.getNomeProduto()
                        + " (disponível: " + produto.getQuantidade() + " un.).");
            }
            ItemEncomenda novo = new ItemEncomenda(0, item.getQuantidadeItem(), item.getPrecoMomento());
            novo.setProduto(produto);
            itens.add(novo);
        }

        Encomenda encomenda = EncomendaFactory.criarEncomenda(
                0,
                cliente,
                dados.getStatus(),
                dados.getValorEntrada(),
                dados.isRetirada(),
                itens,
                dados.getPagamento(),
                dados.isRetirada() ? null : dados.getEntrega()
        );
        encomenda.setDataEntrega(dados.getDataEntrega());
        encomenda.setHoraEntrega(dados.getHoraEntrega().trim());
        encomenda.setDestinatario(dados.getDestinatario().trim());
        encomenda.setObservacao(dados.getObservacao() == null ? "" : dados.getObservacao().trim());

        if (dados.getValorEntrada() > encomenda.getValorTotal()) {
            throw new RegraNegocioException("O valor de entrada não pode ser maior que o valor total da encomenda.");
        }
        return encomenda;
    }

    private void baixarEstoque(Encomenda encomenda) {
        for (ItemEncomenda item : encomenda.getItens()) {
            Produto produto = produtoRepository.buscarPorId(item.getProduto().getId());
            if (produto == null) {
                throw new RegraNegocioException("Produto não encontrado.");
            }
            int restante = produto.getQuantidade() - item.getQuantidadeItem();
            if (restante < 0) {
                throw new RegraNegocioException("Estoque insuficiente de " + produto.getNomeProduto()
                        + " (disponível: " + produto.getQuantidade() + " un.).");
            }
            produtoRepository.atualizarQuantidade(produto.getId(), restante);
        }
    }

    private void devolverEstoque(Encomenda encomenda) {
        for (ItemEncomenda item : encomenda.getItens()) {
            Produto produto = produtoRepository.buscarPorId(item.getProduto().getId());
            if (produto != null) {
                produtoRepository.atualizarQuantidade(produto.getId(), produto.getQuantidade() + item.getQuantidadeItem());
            }
        }
    }
}
