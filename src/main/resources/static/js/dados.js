/* ==========================================================================
   SnapSales - dados.js   (FRONT-END / PROTÓTIPO)

   Dados de EXEMPLO fixos, só para as telas terem o que mostrar e poderem ser
   testadas nesta etapa.
     - NÃO salva nada: tudo fica na memória da página. Ao recarregar ou trocar
       de tela, os dados voltam ao exemplo original.
     - NÃO tem regras de negócio (estoque, saldo real, etc.).

   Na etapa do back-end (Spring), as funções "listar...", "salvar...",
   "excluir..." deste arquivo serão trocadas por chamadas fetch() aos
   Controllers, e os dados virão do banco.
   ========================================================================== */
const Dados = (function () {

    const CHAVES = {
        clientes: 'clientes',
        produtos: 'produtos',
        categorias: 'categorias',
        encomendas: 'encomendas',
        movimentacoes: 'movimentacoes'
    };

    const STATUS_TEXTO = {
        preparacao: 'Em preparação',
        pronto: 'Pronto',
        entregue: 'Entregue'
    };

    /* ---------- utilitários de exibição (ficam no front) ---------- */
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

    /* ---------- memória (só enquanto a página está aberta) ---------- */
    const BANCO = {};
    const copia = x => JSON.parse(JSON.stringify(x));

    function gravar(chave, valor) {
        BANCO[chave] = copia(valor);
    }

    // Na primeira leitura, usa os dados de exemplo
    function ler(chave, semente) {
        if (BANCO[chave] === undefined) BANCO[chave] = semente();
        return copia(BANCO[chave]);
    }

    // No back-end quem faz isso é o banco (auto increment)
    const proximoId = lista => lista.length ? Math.max(...lista.map(x => x.id)) + 1 : 1;
    const codigoDe = id => String(id).padStart(4, '0');

    /* ======================================================================
       DADOS DE EXEMPLO (primeira abertura)
       ====================================================================== */
    const CAT = {
        salgados: { id: 1, nome: 'Salgados' },
        doces: { id: 2, nome: 'Doces' },
        bebidas: { id: 3, nome: 'Bebidas' },
        bolos: { id: 4, nome: 'Bolos e Tortas' }
    };

    const sementeCategorias = () => Object.values(CAT);

    const sementeProdutos = () => [
        { id: 1, codigo: '0001', nome: 'Coxinha de Frango', categoria: CAT.salgados, quantidade: 120, precoUni: 1.50, ativo: true },
        { id: 2, codigo: '0002', nome: 'Pastel de Carne', categoria: CAT.salgados, quantidade: 80, precoUni: 2.00, ativo: true },
        { id: 3, codigo: '0003', nome: 'Brigadeiro', categoria: CAT.doces, quantidade: 200, precoUni: 2.00, ativo: true },
        { id: 4, codigo: '0004', nome: 'Beijinho', categoria: CAT.doces, quantidade: 150, precoUni: 2.00, ativo: true },
        { id: 5, codigo: '0005', nome: 'Refrigerante Lata', categoria: CAT.bebidas, quantidade: 48, precoUni: 5.00, ativo: true },
        { id: 6, codigo: '0006', nome: 'Suco Natural 500ml', categoria: CAT.bebidas, quantidade: 6, precoUni: 7.50, ativo: true },
        { id: 7, codigo: '0007', nome: 'Bolo de Cenoura', categoria: CAT.bolos, quantidade: 10, precoUni: 40.00, ativo: true },
        { id: 8, codigo: '0008', nome: 'Torta de Limão', categoria: CAT.bolos, quantidade: 4, precoUni: 55.00, ativo: false }
    ];

    const sementeClientes = () => [
        { id: 1, codigo: '0001', nome: 'Maria Silva', telefone: '51 999991111', endereco: 'Rua das Flores, 120' },
        { id: 2, codigo: '0002', nome: 'João Pereira', telefone: '51 988882222', endereco: 'Av. Brasil, 450' },
        { id: 3, codigo: '0003', nome: 'Ana Souza', telefone: '51 977773333', endereco: 'Rua Central, 33' },
        { id: 4, codigo: '0004', nome: 'Carlos Mendes', telefone: '51 966664444', endereco: 'Travessa do Sol, 78' },
        { id: 5, codigo: '0005', nome: 'Fernanda Lima', telefone: '51 955555555', endereco: 'Rua XV de Novembro, 910' }
    ];

    const sementeEncomendas = () => [
        {
            id: 1, codigo: '0001', clienteId: 1, cliente: 'Maria Silva', telefone: '51 999991111',
            itens: [
                { produtoId: 1, produto: 'Coxinha de Frango', qtd: 50, preco: 1.50 },
                { produtoId: 3, produto: 'Brigadeiro', qtd: 40, preco: 2.00 }
            ],
            retirada: false, endereco: 'Rua das Flores, 120', destinatario: 'Maria Silva',
            dataEntrega: hojeISO(2), horaEntrega: '15:00', observacao: 'Festa de aniversário. Entregar no salão.',
            pagamento: 'Pix', frete: 10, entrada: 50, status: 'preparacao', criadoEm: hojeISO()
        },
        {
            id: 2, codigo: '0002', clienteId: 2, cliente: 'João Pereira', telefone: '51 988882222',
            itens: [{ produtoId: 5, produto: 'Refrigerante Lata', qtd: 24, preco: 5.00 }],
            retirada: true, endereco: '', destinatario: 'João Pereira',
            dataEntrega: hojeISO(1), horaEntrega: '18:30', observacao: '',
            pagamento: 'Dinheiro', frete: 0, entrada: 0, status: 'pronto', criadoEm: hojeISO(-1)
        },
        {
            id: 3, codigo: '0003', clienteId: 3, cliente: 'Ana Souza', telefone: '51 977773333',
            itens: [
                { produtoId: 7, produto: 'Bolo de Cenoura', qtd: 2, preco: 40.00 },
                { produtoId: 4, produto: 'Beijinho', qtd: 30, preco: 2.00 }
            ],
            retirada: false, endereco: 'Rua Central, 33', destinatario: 'Pedro Souza',
            dataEntrega: hojeISO(-3), horaEntrega: '10:00', observacao: 'Sem coco no bolo.',
            pagamento: 'Cartão', frete: 8, entrada: 0, status: 'entregue', criadoEm: hojeISO(-6)
        },
        {
            id: 4, codigo: '0004', clienteId: 4, cliente: 'Carlos Mendes', telefone: '51 966664444',
            itens: [{ produtoId: 2, produto: 'Pastel de Carne', qtd: 60, preco: 2.00 }],
            retirada: false, endereco: 'Travessa do Sol, 78', destinatario: 'Carlos Mendes',
            dataEntrega: hojeISO(5), horaEntrega: '12:00', observacao: 'Ligar ao chegar.',
            pagamento: 'Pix', frete: 12, entrada: 60, status: 'preparacao', criadoEm: hojeISO()
        },
        {
            id: 5, codigo: '0005', clienteId: 5, cliente: 'Fernanda Lima', telefone: '51 955555555',
            itens: [
                { produtoId: 6, produto: 'Suco Natural 500ml', qtd: 6, preco: 7.50 },
                { produtoId: 1, produto: 'Coxinha de Frango', qtd: 30, preco: 1.50 }
            ],
            retirada: true, endereco: '', destinatario: 'Fernanda Lima',
            dataEntrega: hojeISO(), horaEntrega: '17:00', observacao: '',
            pagamento: 'Pix', frete: 0, entrada: 20, status: 'pronto', criadoEm: hojeISO(-2)
        },
        {
            id: 6, codigo: '0006', clienteId: 1, cliente: 'Maria Silva', telefone: '51 999991111',
            itens: [{ produtoId: 3, produto: 'Brigadeiro', qtd: 100, preco: 2.00 }],
            retirada: false, endereco: 'Rua das Flores, 120', destinatario: 'Maria Silva',
            dataEntrega: hojeISO(-10), horaEntrega: '09:00', observacao: '',
            pagamento: 'Dinheiro', frete: 10, entrada: 0, status: 'entregue', criadoEm: hojeISO(-12)
        }
    ];

    const sementeMovimentacoes = () => [
        {
            id: 1, tipo: 'Entrada', data: hojeISO(-4), descricao: 'Reposição de salgados',
            itens: [
                { produtoId: 1, produto: 'Coxinha de Frango', qtd: 100, preco: 1.50, total: 150 },
                { produtoId: 2, produto: 'Pastel de Carne', qtd: 50, preco: 2.00, total: 100 }
            ]
        },
        {
            id: 2, tipo: 'Entrada', data: hojeISO(-8), descricao: 'Compra de bebidas',
            itens: [{ produtoId: 5, produto: 'Refrigerante Lata', qtd: 48, preco: 5.00, total: 240 }]
        },
        {
            id: 3, tipo: 'Saída', data: hojeISO(-1), descricao: 'Perda: produto vencido',
            itens: [{ produtoId: 3, produto: 'Brigadeiro', qtd: 10, preco: 2.00, total: 20 }]
        },
        {
            id: 4, tipo: 'Saída', data: hojeISO(), descricao: 'Degustação no balcão',
            itens: [{ produtoId: 4, produto: 'Beijinho', qtd: 5, preco: 2.00, total: 10 }]
        }
    ];

    /* ======================================================================
       CLIENTES   (back-end: GET/POST/PUT/DELETE /clientes)
       ====================================================================== */
    const listarClientes = () => ler(CHAVES.clientes, sementeClientes);

    // Sem dados.id = cria | com dados.id = atualiza
    function salvarCliente(dados) {
        const lista = listarClientes();
        let cliente;
        if (dados.id != null) {
            cliente = lista.find(c => c.id === dados.id);
            Object.assign(cliente, dados);
        } else {
            const id = proximoId(lista);
            cliente = Object.assign({}, dados, { id: id, codigo: codigoDe(id) });
            lista.push(cliente);
        }
        gravar(CHAVES.clientes, lista);
        return cliente;
    }

    function excluirCliente(id) {
        gravar(CHAVES.clientes, listarClientes().filter(c => c.id !== id));
    }

    /* ======================================================================
       PRODUTOS E CATEGORIAS   (back-end: /produtos, /categorias)
       ====================================================================== */
    const listarProdutos = () => ler(CHAVES.produtos, sementeProdutos);
    const listarCategorias = () => ler(CHAVES.categorias, sementeCategorias);

    function salvarProduto(dados) {
        const lista = listarProdutos();
        let produto;
        if (dados.id != null) {
            produto = lista.find(p => p.id === dados.id);
            Object.assign(produto, dados);
        } else {
            const id = proximoId(lista);
            produto = Object.assign({ ativo: true }, dados, { id: id, codigo: codigoDe(id) });
            lista.push(produto);
        }
        gravar(CHAVES.produtos, lista);
        return produto;
    }

    function alternarAtivoProduto(id) {
        const lista = listarProdutos();
        const p = lista.find(x => x.id === id);
        if (p) { p.ativo = !p.ativo; gravar(CHAVES.produtos, lista); }
    }

    function excluirProduto(id) {
        gravar(CHAVES.produtos, listarProdutos().filter(p => p.id !== id));
    }

    // Devolve a categoria com esse nome; se não existir, cria
    function obterOuCriarCategoria(nome) {
        const lista = listarCategorias();
        let cat = lista.find(c => c.nome.toLowerCase() === nome.toLowerCase());
        if (!cat) {
            cat = { id: proximoId(lista), nome: nome };
            lista.push(cat);
            gravar(CHAVES.categorias, lista);
        }
        return cat;
    }

    // Lista resumida usada nos formulários (encomenda e movimentação)
    function catalogo() {
        return listarProdutos().map(p => ({
            id: p.id, nome: p.nome, preco: p.precoUni, quantidade: p.quantidade, ativo: p.ativo
        }));
    }

    /* ======================================================================
       MOVIMENTAÇÕES MANUAIS (entradas e saídas)   (back-end: /movimentacoes)
       ====================================================================== */
    const listarMovimentacoesManuais = () => ler(CHAVES.movimentacoes, sementeMovimentacoes);

    function salvarMovimentacao(dados) {
        const lista = listarMovimentacoesManuais();
        let mov;
        if (dados.id != null) {
            mov = lista.find(m => m.id === dados.id);
            Object.assign(mov, dados);
        } else {
            mov = Object.assign({}, dados, { id: proximoId(lista) });
            lista.push(mov);
        }
        gravar(CHAVES.movimentacoes, lista);
        return mov;
    }

    function excluirMovimentacao(id) {
        gravar(CHAVES.movimentacoes, listarMovimentacoesManuais().filter(m => m.id !== id));
    }

    /* ======================================================================
       ENCOMENDAS   (back-end: /encomendas)
       ====================================================================== */
    const lerEncomendas = () => ler(CHAVES.encomendas, sementeEncomendas);

    const limparItens = itens => itens.map(i => ({
        produtoId: i.produtoId, produto: i.produto, qtd: i.qtd, preco: i.preco
    }));

    function listarEncomendas() {
        return lerEncomendas().slice().sort((a, b) => b.id - a.id);
    }

    function buscarEncomenda(id) {
        return lerEncomendas().find(e => e.id === id) || null;
    }

    // Toda encomenda nova começa "em preparação"
    function adicionarEncomenda(dados) {
        const lista = lerEncomendas();
        const id = proximoId(lista);
        const nova = Object.assign({}, dados, {
            id: id,
            codigo: codigoDe(id),
            itens: limparItens(dados.itens),
            status: 'preparacao',
            criadoEm: hojeISO()
        });
        lista.push(nova);
        gravar(CHAVES.encomendas, lista);
        return nova;
    }

    function atualizarEncomenda(id, dados) {
        const lista = lerEncomendas();
        const idx = lista.findIndex(e => e.id === id);
        const antiga = lista[idx];
        lista[idx] = Object.assign({}, antiga, dados, {
            id: antiga.id,
            codigo: antiga.codigo,
            itens: limparItens(dados.itens),
            status: antiga.status,
            criadoEm: antiga.criadoEm
        });
        gravar(CHAVES.encomendas, lista);
        return lista[idx];
    }

    function excluirEncomenda(id) {
        gravar(CHAVES.encomendas, lerEncomendas().filter(e => e.id !== id));
    }

    function alterarStatus(id, status) {
        const lista = lerEncomendas();
        const enc = lista.find(e => e.id === id);
        if (!enc || !STATUS_TEXTO[status]) return;
        enc.status = status;
        gravar(CHAVES.encomendas, lista);
    }

    // Monta os textos que a tela de encomendas exibe
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
    function listarVendas() {
        return lerEncomendas().map(e => ({
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

    /* ---------- API pública ---------- */
    const api = {
        esc, moeda, dataBR, hojeISO, STATUS_TEXTO,
        listarClientes, salvarCliente, excluirCliente,
        listarProdutos, salvarProduto, alternarAtivoProduto, excluirProduto,
        listarCategorias, obterOuCriarCategoria,
        listarMovimentacoesManuais, salvarMovimentacao, excluirMovimentacao,
        listarEncomendas, buscarEncomenda, adicionarEncomenda, atualizarEncomenda,
        excluirEncomenda, alterarStatus, formatarEncomenda, listarVendas
    };

    Object.defineProperty(api, 'catalogo', { get: catalogo });

    return api;
})();
