package br.com.snapsales.repositories;

import br.com.snapsales.models.Movimentacao;
import br.com.snapsales.models.MovimentacaoEstoque;
import br.com.snapsales.models.Produto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Movimentacao = 1 linha em "movimentacao" + N linhas em "item_movimentacao". */
@Repository
public class MovimentacaoRepositoryJdbc implements MovimentacaoRepository {

    private static final String SELECT = "SELECT id, entrada, data_mov, descricao FROM movimentacao ";

    private final JdbcTemplate jdbc;

    public MovimentacaoRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public long salvar(Movimentacao m) {
        long id = Insercao.inserir(jdbc,
                "INSERT INTO movimentacao (entrada, data_mov, descricao) VALUES (?, ?, ?)",
                m.isEntrada(), m.getData(), m.getDescricao());
        inserirItens(id, m.getItens());
        return id;
    }

    @Override
    @Transactional
    public boolean atualizar(long id, Movimentacao m) {
        int linhas = jdbc.update("UPDATE movimentacao SET entrada = ?, data_mov = ?, descricao = ? WHERE id = ?",
                m.isEntrada(), m.getData(), m.getDescricao(), id);
        if (linhas == 0) {
            return false;
        }
        jdbc.update("DELETE FROM item_movimentacao WHERE movimentacao_id = ?", id);
        inserirItens(id, m.getItens());
        return true;
    }

    @Override
    public boolean remover(long id) {
        return jdbc.update("DELETE FROM movimentacao WHERE id = ?", id) > 0;
    }

    @Override
    public List<Movimentacao> listar() {
        List<Movimentacao> lista = jdbc.query(SELECT + "ORDER BY id DESC", this::mapear);
        for (Movimentacao m : lista) {
            m.setItens(carregarItens(m));
        }
        return lista;
    }

    @Override
    public Movimentacao buscarPorId(long id) {
        List<Movimentacao> lista = jdbc.query(SELECT + "WHERE id = ?", this::mapear, id);
        if (lista.isEmpty()) {
            return null;
        }
        Movimentacao m = lista.get(0);
        m.setItens(carregarItens(m));
        return m;
    }

    /* ---------------------------------------------------------------- */

    private void inserirItens(long movimentacaoId, List<MovimentacaoEstoque> itens) {
        for (MovimentacaoEstoque item : itens) {
            jdbc.update("INSERT INTO item_movimentacao (movimentacao_id, produto_id, quantidade, preco_unit) "
                    + "VALUES (?, ?, ?, ?)",
                    movimentacaoId, item.getProduto().getId(), item.getQuantidade(), item.getPrecoUnitario());
        }
    }

    private List<MovimentacaoEstoque> carregarItens(Movimentacao m) {
        return jdbc.query(
                "SELECT i.id, i.quantidade, i.preco_unit, p.id AS prod_id, p.nome AS prod_nome "
                + "FROM item_movimentacao i JOIN produto p ON p.id = i.produto_id "
                + "WHERE i.movimentacao_id = ? ORDER BY i.id",
                (rs, n) -> {
                    Produto produto = new Produto();
                    produto.setId(rs.getLong("prod_id"));
                    produto.setNomeProduto(rs.getString("prod_nome"));
                    MovimentacaoEstoque linha = new MovimentacaoEstoque(
                            rs.getLong("id"), produto, rs.getInt("quantidade"), m.isEntrada());
                    linha.setPrecoUnitario(rs.getDouble("preco_unit"));
                    return linha;
                },
                m.getId());
    }

    private Movimentacao mapear(ResultSet rs, int n) throws SQLException {
        Movimentacao m = new Movimentacao();
        m.setId(rs.getLong("id"));
        m.setEntrada(rs.getBoolean("entrada"));
        m.setData(rs.getObject("data_mov", LocalDate.class));
        m.setDescricao(rs.getString("descricao"));
        return m;
    }
}
