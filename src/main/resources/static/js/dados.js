/* ==========================================================================
   SnapSales - dados.js   (comunicação com o back-end)

   Antes (protótipo): guardava dados de exemplo na memória da página.
   Agora: cada função faz uma chamada fetch() aos Controllers do Spring
   (rotas /api/...) e os dados vêm do banco.

   ATENÇÃO: as funções que falam com o servidor são ASSÍNCRONAS (devolvem
   Promise). Nas telas elas são usadas com "await" dentro de try/catch;
   se o servidor recusar algo (ex.: estoque insuficiente), a mensagem dele
   chega aqui como Error e a tela mostra com alert(e.message).
   ========================================================================== */
const Dados = (function () {

    const STATUS_TEXTO = {
        preparacao: 'Em preparação',
        pronto: 'Pronto',
        entregue: 'Entregue'
    };

    /* ---------- utilitários de exibição (continuam no front) ---------- */
    const arredonda = n => Math.round(n * 100) / 100;
    const moeda = n => Number(n || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const dataBR = iso => iso ? iso.split('-').reverse().join('/') : '';
    const somaItens = itens => itens.reduce((s, i) => s + arredonda(i.qtd * i.preco), 0);

    // Evita que texto digitado vire HTML na tela
    const esc = t => String(t == null ? '' : t)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');

    // Data de hoje (ou de hoje + n dias) no formato AAAA-MM-DD
    function hojeISO(diasAMais) {
        const d = new Date();
        d.setDate(d.getDate() + (diasAMais || 0));
        d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
        return d.toISOString().slice(0, 10);
    }

    /* ---------- chamada HTTP ---------- */
    async function http(metodo, url, corpo) {
        const opcoes = { method: metodo, headers: { 'Accept': 'application/json' } };
        if (corpo !== undefined) {
            opcoes.headers['Content-Type'] = 'application/json';
            opcoes.body = JSON.stringify(corpo);
        }

        let resposta;
        try {
            resposta = await fetch(url, opcoes);
        } catch (e) {
            throw new Error('Não foi possível falar com o servidor. Ele está rodando?');
        }

        if (resposta.status === 204) return null;

        let dados = null;
        try { dados = await resposta.json(); } catch (e) { /* resposta sem corpo */ }

        if (!resposta.ok) {
            throw new Error((dados && dados.erro) || ('Erro ' + resposta.status + ' ao falar com o servidor.'));
        }
        return dados;
    }

    /* ======================================================================
       CLIENTES   (GET/POST/PUT/DELETE /api/clientes)
       ====================================================================== */
    const listarClientes = () => http('GET', '/api/clientes');

    // Sem dados.id = cria | com dados.id = atualiza
    const salvarCliente = dados => dados.id != null
        ? http('PUT', '/api/clientes/' + dados.id, dados)
        : http('POST', '/api/clientes', dados);

    const excluirCliente = id => http('DELETE', '/api/clientes/' + id);

    /* ======================================================================
       PRODUTOS E CATEGORIAS   (/api/produtos, /api/categorias)
       ====================================================================== */
    const listarProdutos = () => http('GET', '/api/produtos');
    const listarCategorias = () => http('GET', '/api/categorias');

    const salvarProduto = dados => dados.id != null
        ? http('PUT', '/api/produtos/' + dados.id, dados)
        : http('POST', '/api/produtos', dados);

    const alternarAtivoProduto = id => http('PATCH', '/api/produtos/' + id + '/ativo');
    const excluirProduto = id => http('DELETE', '/api/produtos/' + id);

    // Devolve a categoria com esse nome; se não existir, o servidor cria
    const obterOuCriarCategoria = nome => http('POST', '/api/categorias', { nome: nome });

    // Lista resumida usada nos formulários (encomenda e movimentação)
    async function catalogo() {
        const produtos = await listarProdutos();
        return produtos.map(p => ({
            id: p.id, nome: p.nome, preco: p.precoUni, quantidade: p.quantidade, ativo: p.ativo
        }));
    }

    /* ======================================================================
       MOVIMENTAÇÕES MANUAIS (entradas e saídas)   (/api/movimentacoes)
       ====================================================================== */
    const listarMovimentacoesManuais = () => http('GET', '/api/movimentacoes');

    const salvarMovimentacao = dados => dados.id != null
        ? http('PUT', '/api/movimentacoes/' + dados.id, dados)
        : http('POST', '/api/movimentacoes', dados);

    const excluirMovimentacao = id => http('DELETE', '/api/movimentacoes/' + id);

    /* ======================================================================
       ENCOMENDAS   (/api/encomendas)
       ====================================================================== */
    const listarEncomendas = () => http('GET', '/api/encomendas');
    const buscarEncomenda = id => http('GET', '/api/encomendas/' + id);
    const adicionarEncomenda = dados => http('POST', '/api/encomendas', dados);
    const atualizarEncomenda = (id, dados) => http('PUT', '/api/encomendas/' + id, dados);
    const excluirEncomenda = id => http('DELETE', '/api/encomendas/' + id);
    const alterarStatus = (id, status) => http('PATCH', '/api/encomendas/' + id + '/status', { status: status });

    // Monta os textos que a tela de encomendas exibe (só formatação, não chama o servidor)
    function formatarEncomenda(e) {
        const sub = arredonda(somaItens(e.itens));
        const frete = e.frete || 0;
        const entrada = e.entrada || 0;
        const total = arredonda(sub + frete);
        const aPagar = Math.max(arredonda(total - entrada), 0);

        return {
            id: e.id,
            codigo: e.codigo,
            cliente: e.cliente,
            telefone: e.telefone,
            status: e.status,
            statusTexto: STATUS_TEXTO[e.status] || e.status,
            dataEntrega: dataBR(e.dataEntrega) + ' às ' + e.horaEntrega,
            valorTotal: moeda(total),
            totalItens: moeda(sub),
            itens: e.itens.map(i => i.qtd + 'x ' + i.produto + ' - ' + moeda(i.qtd * i.preco)),
            endereco: e.retirada ? 'Retirada no local' : e.endereco,
            destinatario: e.destinatario ? 'Destinatário: ' + e.destinatario : '',
            descricao: e.observacao || '',
            frete: 'Frete: ' + moeda(frete),
            subtotal: 'Itens: ' + moeda(sub),
            entrada: 'Entrada: ' + (entrada > 0 ? '-' : '') + moeda(entrada),
            aPagar: 'A pagar: ' + moeda(aPagar)
        };
    }

    // Cada encomenda aparece como "Venda" na tela de Movimentação
    function vendasDasEncomendas(encomendas) {
        return encomendas.map(e => ({
            id: 'v' + e.id,
            tipo: 'Venda',
            data: e.criadoEm || e.dataEntrega,
            descricao: 'Encomenda ' + e.codigo + ' - ' + e.cliente,
            itens: e.itens.map(i => ({
                produtoId: i.produtoId,
                produto: i.produto,
                qtd: i.qtd,
                preco: i.preco,
                total: arredonda(i.qtd * i.preco)
            })),
            frete: e.frete || 0,
            cliente: e.cliente,
            pagamento: e.pagamento,
            entrega: e.retirada
                ? 'Retirada no local - ' + dataBR(e.dataEntrega) + ' às ' + e.horaEntrega
                : e.endereco + ' - ' + dataBR(e.dataEntrega) + ' às ' + e.horaEntrega,
            observacao: e.observacao
        }));
    }

    const listarVendas = async () => vendasDasEncomendas(await listarEncomendas());

    /* ---------- API pública ---------- */
    return {
        esc, moeda, dataBR, hojeISO, STATUS_TEXTO,
        listarClientes, salvarCliente, excluirCliente,
        listarProdutos, salvarProduto, alternarAtivoProduto, excluirProduto,
        listarCategorias, obterOuCriarCategoria, catalogo,
        listarMovimentacoesManuais, salvarMovimentacao, excluirMovimentacao,
        listarEncomendas, buscarEncomenda, adicionarEncomenda, atualizarEncomenda,
        excluirEncomenda, alterarStatus, formatarEncomenda, listarVendas
    };
})();

// Qualquer erro vindo do servidor que não foi tratado numa tela aparece como aviso para a pessoa
window.addEventListener('unhandledrejection', function (evento) {
    evento.preventDefault();
    alert((evento.reason && evento.reason.message) || 'Ocorreu um erro inesperado.');
});
