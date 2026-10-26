package br.com.snapsales.repositories;

import br.com.snapsales.models.Clientes;
import br.com.snapsales.models.Encomenda;
import br.com.snapsales.models.Entrega;
import br.com.snapsales.models.ItemEncomenda;
import br.com.snapsales.models.Pagamentos;
import br.com.snapsales.models.Produto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Encomenda = 1 linha na tabela "encomenda" + N linhas em "item_encomenda".
 * Pagamento e Entrega nao tem tabela propria: ficam em colunas da encomenda.
 */
@Repository
public class EncomendaRepositoryJdbc implements EncomendaRepository {

    private static final String SELECT =
            "SELECT e.id, e.status, e.retirada, e.endereco_entrega, e.destinatario, e.frete, "
            + "e.data_entrega, e.hora_entrega, e.observacao, e.forma_pagamento, e.valor_entrada, "
            + "e.valor_total, e.criado_em, "
            + "c.id AS cli_id, c.nome AS cli_nome, c.telefone AS cli_telefone, c.endereco AS cli_endereco "
            + "FROM encomenda e JOIN cliente c ON c.id = e.cliente_id ";

    private final JdbcTemplate jdbc;

    public EncomendaRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public long salvar(Encomenda e) {
        long id = Insercao.inserir(jdbc,
                "INSERT INTO encomenda (cliente_id, status, retirada, endereco_entrega, destinatario, frete, "
                + "data_entrega, hora_entrega, observacao, forma_pagamento, valor_entrada, valor_total, criado_em) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getCliente().getId(), e.getStatus(), e.isRetirada(), enderecoEntrega(e), e.getDestinatario(),
                frete(e), e.getDataEntrega(), e.getHoraEntrega(), e.getObservacao(),
                e.getPagamento().getFormaPagamento(), e.getValorEntrada(), e.getValorTotal(), e.getCriadoEm());
        inserirItens(id, e.getItens());
        return id;
    }

    @Override
    @Transactional
    public boolean atualizar(long id, Encomenda e) {
        int linhas = jdbc.update(
                "UPDATE encomenda SET cliente_id = ?, retirada = ?, endereco_entrega = ?, destinatario = ?, "
                + "frete = ?, data_entrega = ?, hora_entrega = ?, observacao = ?, forma_pagamento = ?, "
                + "valor_entrada = ?, valor_total = ? WHERE id = ?",
                e.getCliente().getId(), e.isRetirada(), enderecoEntrega(e), e.getDestinatario(), frete(e),
                e.getDataEntrega(), e.getHoraEntrega(), e.getObservacao(), e.getPagamento().getFormaPagamento(),
                e.getValorEntrada(), e.getValorTotal(), id);
        if (linhas == 0) {
            return false;
        }
        jdbc.update("DELETE FROM item_encomenda WHERE encomenda_id = ?", id);
        inserirItens(id, e.getItens());
        return true;
    }

    @Override
    public boolean atualizarStatus(long id, String status) {
        return jdbc.update("UPDATE encomenda SET status = ? WHERE id = ?", status, id) > 0;
    }

    @Override
    public boolean remover(long id) {
        // os itens saem junto (ON DELETE CASCADE no schema.sql)
        return jdbc.update("DELETE FROM encomenda WHERE id = ?", id) > 0;
    }

    @Override
    public List<Encomenda> listar() {
        List<Encomenda> lista = jdbc.query(SELECT + "ORDER BY e.id DESC", this::mapear);
        for (Encomenda e : lista) {
            e.setItens(carregarItens(e.getId()));
        }
        return lista;
    }

    @Override
    public Encomenda buscarPorId(long id) {
        List<Encomenda> lista = jdbc.query(SELECT + "WHERE e.id = ?", this::mapear, id);
        if (lista.isEmpty()) {
            return null;
        }
        Encomenda e = lista.get(0);
        e.setItens(carregarItens(id));
        return e;
    }

    /* ---------------------------------------------------------------- */

    private void inserirItens(long encomendaId, List<ItemEncomenda> itens) {
        for (ItemEncomenda item : itens) {
            jdbc.update("INSERT INTO item_encomenda (encomenda_id, produto_id, quantidade, preco_momento) "
                    + "VALUES (?, ?, ?, ?)",
                    encomendaId, item.getProduto().getId(), item.getQuantidadeItem(), item.getPrecoMomento());
        }
    }

    private List<ItemEncomenda> carregarItens(long encomendaId) {
        return jdbc.query(
                "SELECT i.id, i.quantidade, i.preco_momento, p.id AS prod_id, p.nome AS prod_nome "
                + "FROM item_encomenda i JOIN produto p ON p.id = i.produto_id "
                + "WHERE i.encomenda_id = ? ORDER BY i.id",
                (rs, n) -> {
                    ItemEncomenda item = new ItemEncomenda(
                            rs.getLong("id"), rs.getInt("quantidade"), rs.getDouble("preco_momento"));
                    Produto produto = new Produto();
                    produto.setId(rs.getLong("prod_id"));
                    produto.setNomeProduto(rs.getString("prod_nome"));
                    item.setProduto(produto);
                    return item;
                },
                encomendaId);
    }

    private Encomenda mapear(ResultSet rs, int n) throws SQLException {
        Clientes cliente = new Clientes(rs.getLong("cli_id"), rs.getString("cli_nome"),
                rs.getString("cli_telefone"), rs.getString("cli_endereco"));
        Encomenda e = new Encomenda(rs.getLong("id"), rs.getString("status"), cliente,
                rs.getDouble("valor_entrada"), rs.getDouble("valor_total"), rs.getBoolean("retirada"));
        e.setPagamento(new Pagamentos(e.getId(), rs.getString("forma_pagamento")));
        e.setDestinatario(rs.getString("destinatario"));
        e.setDataEntrega(rs.getObject("data_entrega", LocalDate.class));
        e.setHoraEntrega(rs.getString("hora_entrega"));
        e.setObservacao(rs.getString("observacao"));
        e.setCriadoEm(rs.getObject("criado_em", LocalDate.class));
        if (!e.isRetirada()) {
            e.setEntrega(new Entrega(e.getId(), rs.getString("endereco_entrega"), e.getDestinatario(),
                    rs.getDouble("frete"), e.getDataEntrega()));
        }
        return e;
    }

    private static String enderecoEntrega(Encomenda e) {
        return e.isRetirada() || e.getEntrega() == null ? null : e.getEntrega().getEndereco();
    }

    private static double frete(Encomenda e) {
        return e.isRetirada() || e.getEntrega() == null ? 0.0 : e.getEntrega().getFrete();
    }
}
