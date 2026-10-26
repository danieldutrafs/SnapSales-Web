package br.com.snapsales.repositories;

import br.com.snapsales.models.Clientes;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Guarda clientes no banco usando SQL (JDBC). A classe Clientes continua sem anotacoes. */
@Repository
public class ClienteRepositoryJdbc implements ClienteRepository {

    private static final RowMapper<Clientes> MAPEADOR = (rs, n) -> new Clientes(
            rs.getLong("id"),
            rs.getString("nome"),
            rs.getString("telefone"),
            rs.getString("endereco"));

    private final JdbcTemplate jdbc;

    public ClienteRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long salvar(Clientes c) {
        return Insercao.inserir(jdbc,
                "INSERT INTO cliente (nome, telefone, endereco) VALUES (?, ?, ?)",
                c.getNomeCliente(), c.getTelefone(), c.getEndereco());
    }

    @Override
    public boolean atualizar(long id, Clientes c) {
        return jdbc.update("UPDATE cliente SET nome = ?, telefone = ?, endereco = ? WHERE id = ?",
                c.getNomeCliente(), c.getTelefone(), c.getEndereco(), id) > 0;
    }

    @Override
    public boolean remover(long id) {
        return jdbc.update("DELETE FROM cliente WHERE id = ?", id) > 0;
    }

    @Override
    public List<Clientes> listar() {
        return jdbc.query("SELECT id, nome, telefone, endereco FROM cliente ORDER BY id", MAPEADOR);
    }

    @Override
    public Clientes buscarPorId(long id) {
        List<Clientes> lista = jdbc.query(
                "SELECT id, nome, telefone, endereco FROM cliente WHERE id = ?", MAPEADOR, id);
        return lista.isEmpty() ? null : lista.get(0);
    }
}
