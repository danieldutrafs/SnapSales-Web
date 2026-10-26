package br.com.snapsales.repositories;

import br.com.snapsales.models.Categoria;
import br.com.snapsales.models.Produto;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ProdutoRepositoryJdbc implements ProdutoRepository {

    private static final String SELECT =
            "SELECT p.id, p.nome, p.quantidade, p.preco, p.ativo, c.id AS cat_id, c.nome AS cat_nome "
            + "FROM produto p JOIN categoria c ON c.id = p.categoria_id ";

    private static final RowMapper<Produto> MAPEADOR = (rs, n) -> {
        Produto p = new Produto(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getInt("quantidade"),
                rs.getDouble("preco"),
                new Categoria(rs.getLong("cat_id"), rs.getString("cat_nome")));
        p.setAtivo(rs.getBoolean("ativo"));
        return p;
    };

    private final JdbcTemplate jdbc;

    public ProdutoRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long salvar(Produto p) {
        return Insercao.inserir(jdbc,
                "INSERT INTO produto (nome, quantidade, preco, ativo, categoria_id) VALUES (?, ?, ?, ?, ?)",
                p.getNomeProduto(), p.getQuantidade(), p.getPreco(), p.isAtivo(), p.getCategoria().getId());
    }

    @Override
    public boolean atualizar(long id, Produto p) {
        return jdbc.update(
                "UPDATE produto SET nome = ?, quantidade = ?, preco = ?, categoria_id = ? WHERE id = ?",
                p.getNomeProduto(), p.getQuantidade(), p.getPreco(), p.getCategoria().getId(), id) > 0;
    }

    @Override
    public boolean atualizarAtivo(long id, boolean ativo) {
        return jdbc.update("UPDATE produto SET ativo = ? WHERE id = ?", ativo, id) > 0;
    }

    @Override
    public boolean atualizarQuantidade(long id, int quantidade) {
        return jdbc.update("UPDATE produto SET quantidade = ? WHERE id = ?", quantidade, id) > 0;
    }

    @Override
    public boolean remover(long id) {
        return jdbc.update("DELETE FROM produto WHERE id = ?", id) > 0;
    }

    @Override
    public List<Produto> listar() {
        return jdbc.query(SELECT + "ORDER BY p.id", MAPEADOR);
    }

    @Override
    public Produto buscarPorId(long id) {
        List<Produto> lista = jdbc.query(SELECT + "WHERE p.id = ?", MAPEADOR, id);
        return lista.isEmpty() ? null : lista.get(0);
    }
}
