package br.com.snapsales.repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** Auxiliar: executa um INSERT e devolve o id gerado pelo banco. */
final class Insercao {

    private Insercao() {
    }

    static long inserir(JdbcTemplate jdbc, String sql, Object... parametros) {
        KeyHolder chaves = new GeneratedKeyHolder();
        jdbc.update(conexao -> {
            PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            return ps;
        }, chaves);

        // Procura a coluna "id" entre as chaves devolvidas (sem diferenciar maiusculas/minusculas)
        List<Map<String, Object>> linhas = chaves.getKeyList();
        if (!linhas.isEmpty()) {
            Number primeiroNumero = null;
            for (Map.Entry<String, Object> campo : linhas.get(0).entrySet()) {
                if (campo.getValue() instanceof Number numero) {
                    if ("id".equalsIgnoreCase(campo.getKey())) {
                        return numero.longValue();
                    }
                    if (primeiroNumero == null) {
                        primeiroNumero = numero;
                    }
                }
            }
            if (primeiroNumero != null) {
                return primeiroNumero.longValue();
            }
        }
        throw new IllegalStateException("O banco não devolveu o id gerado.");
    }
}
