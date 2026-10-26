package br.com.snapsales.repositories;

import br.com.snapsales.models.Categoria;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaRepositoryJdbc implements CategoriaRepository {

    private static final RowMapper<Categoria> MAPEADOR =
            (rs, n) -> new Categoria(rs.getLong("id"), rs.getString("nome"));

    private final JdbcTemplate jdbc;

    public CategoriaRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long salvar(Categoria categoria) {
        return Insercao.inserir(jdbc, "INSERT INTO categoria (nome) VALUES (?)", categoria.getNomeCategoria());
    }

    @Override
    public List<Categoria> listar() {
        return jdbc.query("SELECT id, nome FROM categoria ORDER BY id", MAPEADOR);
    }

    @Override
    public Categoria buscarPorId(long id) {
        List<Categoria> lista = jdbc.query("SELECT id, nome FROM categoria WHERE id = ?", MAPEADOR, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Categoria buscarPorNome(String nome) {
        List<Categoria> lista = jdbc.query(
                "SELECT id, nome FROM categoria WHERE LOWER(nome) = LOWER(?)", MAPEADOR, nome);
        return lista.isEmpty() ? null : lista.get(0);
    }
}
