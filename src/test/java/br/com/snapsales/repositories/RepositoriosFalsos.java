package br.com.snapsales.repositories;

import br.com.snapsales.models.Clientes;
import br.com.snapsales.models.Encomenda;
import br.com.snapsales.models.Movimentacao;
import br.com.snapsales.models.Produto;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorios que guardam tudo em memoria. Servem para testar os services sem precisar de banco
 * (so e possivel por causa da inversao de dependencia: o service conhece apenas a interface).
 */
public final class RepositoriosFalsos {

    private RepositoriosFalsos() {
    }

    public static class ClienteFalso implements ClienteRepository {

        private final Map<Long, Clientes> dados = new LinkedHashMap<>();
        private long proximoId = 1;

        @Override
        public long salvar(Clientes c) {
            long id = proximoId++;
            dados.put(id, c);
            return id;
        }

        @Override
        public boolean atualizar(long id, Clientes c) {
            if (!dados.containsKey(id)) {
                return false;
            }
            dados.put(id, c);
            return true;
        }

        @Override
        public boolean remover(long id) {
            return dados.remove(id) != null;
        }

        @Override
        public List<Clientes> listar() {
            return new ArrayList<>(dados.values());
        }

        @Override
        public Clientes buscarPorId(long id) {
            return dados.get(id);
        }
    }

    public static class ProdutoFalso implements ProdutoRepository {

        private final Map<Long, Produto> dados = new LinkedHashMap<>();
        private long proximoId = 1;

        @Override
        public long salvar(Produto p) {
            long id = proximoId++;
            p.setId(id);
            dados.put(id, p);
            return id;
        }

        @Override
        public boolean atualizar(long id, Produto p) {
            if (!dados.containsKey(id)) {
                return false;
            }
            p.setId(id);
            dados.put(id, p);
            return true;
        }

        @Override
        public boolean atualizarAtivo(long id, boolean ativo) {
            Produto p = dados.get(id);
            if (p == null) {
                return false;
            }
            p.setAtivo(ativo);
            return true;
        }

        @Override
        public boolean atualizarQuantidade(long id, int quantidade) {
            Produto p = dados.get(id);
            if (p == null) {
                return false;
            }
            p.setQuantidade(quantidade);
            return true;
        }

        @Override
        public boolean remover(long id) {
            return dados.remove(id) != null;
        }

        @Override
        public List<Produto> listar() {
            return new ArrayList<>(dados.values());
        }

        @Override
        public Produto buscarPorId(long id) {
            return dados.get(id);
        }
    }

    public static class EncomendaFalsa implements EncomendaRepository {

        private final Map<Long, Encomenda> dados = new LinkedHashMap<>();
        private long proximoId = 1;

        @Override
        public long salvar(Encomenda e) {
            long id = proximoId++;
            e.setId(id);
            dados.put(id, e);
            return id;
        }

        @Override
        public boolean atualizar(long id, Encomenda e) {
            if (!dados.containsKey(id)) {
                return false;
            }
            e.setId(id);
            dados.put(id, e);
            return true;
        }

        @Override
        public boolean atualizarStatus(long id, String status) {
            Encomenda e = dados.get(id);
            if (e == null) {
                return false;
            }
            e.setStatus(status);
            return true;
        }

        @Override
        public boolean remover(long id) {
            return dados.remove(id) != null;
        }

        @Override
        public List<Encomenda> listar() {
            return new ArrayList<>(dados.values());
        }

        @Override
        public Encomenda buscarPorId(long id) {
            return dados.get(id);
        }
    }

    public static class MovimentacaoFalsa implements MovimentacaoRepository {

        private final Map<Long, Movimentacao> dados = new LinkedHashMap<>();
        private long proximoId = 1;

        @Override
        public long salvar(Movimentacao m) {
            long id = proximoId++;
            m.setId(id);
            dados.put(id, m);
            return id;
        }

        @Override
        public boolean atualizar(long id, Movimentacao m) {
            if (!dados.containsKey(id)) {
                return false;
            }
            m.setId(id);
            dados.put(id, m);
            return true;
        }

        @Override
        public boolean remover(long id) {
            return dados.remove(id) != null;
        }

        @Override
        public List<Movimentacao> listar() {
            return new ArrayList<>(dados.values());
        }

        @Override
        public Movimentacao buscarPorId(long id) {
            return dados.get(id);
        }
    }
}
