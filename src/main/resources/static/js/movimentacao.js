/* SnapSales - tela de Movimentação (interface: cards, tabela, painel, modal e validações) */
$(function () {

    const moeda = Dados.moeda;
    const dataBR = Dados.dataBR;
    const hojeISO = Dados.hojeISO;
    const esc = Dados.esc;
    const arredonda = n => Math.round(n * 100) / 100;
    const somaItens = itens => itens.reduce((s, i) => s + i.total, 0);
    const valorMov = m => arredonda(somaItens(m.itens) + (m.frete || 0));   // itens + frete (vendas)

    // Card verde: false = total de vendas de todo o período | true = só as vendas do mês atual
    const VENDAS_SOMENTE_DO_MES = false;

    let manuais = [];             // entradas e saídas cadastradas nesta tela
    let movSelecionadaId = null;  // linha aberta no painel
    let movEmEdicaoId = null;     // null = nova | id = editando
    let itensModal = [];          // itens do modal
    let itemEmEdicao = null;      // índice do item sendo editado

    // Manuais + vendas (que vêm das encomendas)
    let vendas = [];              // vendas geradas pelas encomendas (vêm do servidor)
    let catalogo = [];            // produtos (com estoque) vindos do servidor
    const todas = () => manuais.concat(vendas);

    async function carregar() {
        manuais = await Dados.listarMovimentacoesManuais();
        vendas = await Dados.listarVendas();
        catalogo = await Dados.catalogo();
        preencherListaProdutos();
        atualizarCards();
        renderizarTabela();
    }

    function buscarProduto(nome) {
        const n = nome.trim().toLowerCase();
        return catalogo.find(p => p.nome.toLowerCase() === n);
    }

    /* ===================== CARDS ===================== */
    // Verde    = total em vendas
    // Vermelho = perdas e insumos (saídas) do mês atual
    // Azul     = saldo atual = VALOR DO ESTOQUE (quantidade x preço de todos os produtos).
    //            Sobe com entradas e desce com encomendas e saídas, porque o servidor já
    //            atualiza a quantidade de cada produto.
    function atualizarCards() {
        const mesAtual = hojeISO().slice(0, 7);
        const doMes = m => String(m.data).slice(0, 7) === mesAtual;

        let vendasTotal = 0, perdasMes = 0;

        todas().forEach(m => {
            const v = valorMov(m);
            if (m.tipo === 'Saída') {
                if (doMes(m)) perdasMes += v;
            } else if (m.tipo === 'Venda') {
                if (!VENDAS_SOMENTE_DO_MES || doMes(m)) vendasTotal += v;
            }
        });

        const valorEstoque = catalogo.reduce((soma, p) => soma + p.quantidade * p.preco, 0);

        $('#entrada').text(moeda(vendasTotal));
        $('#saida').text(moeda(perdasMes));
        $('#saldo-atual').text(moeda(valorEstoque));
    }

    /* ===================== TABELA ===================== */
    function renderizarTabela() {
        const termo = $('#input-pesquisa').val().toLowerCase().trim();
        const $tbody = $('#tabela-movi-corpo').empty();

        const lista = todas()
            .sort((a, b) => b.data.localeCompare(a.data) ||
                String(b.id).localeCompare(String(a.id), undefined, { numeric: true }))
            .filter(m => [dataBR(m.data), m.tipo, m.descricao, moeda(valorMov(m))].join(' ').toLowerCase().includes(termo));

        if (!lista.length) {
            $tbody.append('<tr><td colspan="4">Nenhuma movimentação encontrada.</td></tr>');
            return;
        }

        lista.forEach(m => {
            const tag = m.tipo === 'Saída' ? 'tag-saida' : (m.tipo === 'Venda' ? 'tag-venda' : 'tag-entrada');
            const valor = valorMov(m) * (m.tipo === 'Saída' ? -1 : 1);
            const sel = m.id === movSelecionadaId ? 'linha-selecionada' : '';
            $tbody.append(`
                <tr data-id="${m.id}" class="${sel}">
                    <td>${dataBR(m.data)}</td>
                    <td><span class="tag ${tag}">${esc(m.tipo)}</span></td>
                    <td>${esc(m.descricao)}</td>
                    <td>${moeda(valor)}</td>
                </tr>`);
        });
    }

    $('#input-pesquisa').on('input', renderizarTabela);

    $('#tabela-movi-corpo').on('click', 'tr', function () {
        const id = $(this).data('id');
        const m = todas().find(x => x.id === id);
        if (m) abrirPainel(m);
    });

    /* ===================== PAINEL LATERAL ===================== */
    const $painel = $('#painel-detalhes');
    const $btnVerDetalhes = $('#btn-ver-detalhes');

    function abrirPainel(m) {
        const total = valorMov(m);
        const ehVenda = m.tipo === 'Venda';

        const $corpo = $('#corpo-tabela-itens').empty();
        m.itens.forEach(i => {
            $corpo.append(`<tr><td>${esc(i.produto)}</td><td>${i.qtd}</td><td>${moeda(i.total)}</td></tr>`);
        });
        if (m.frete) {
            $corpo.append(`<tr><td>Frete</td><td>-</td><td>${moeda(m.frete)}</td></tr>`);
        }
        $('#info-valor-total-itens').text(moeda(total));

        if (ehVenda) {
            $('#painel-titulo').text('Informações da venda');
            $('#info-cliente').text(m.cliente || '-');
            $('#info-data').text(dataBR(m.data));
            $('#info-valor').text(moeda(total) + (m.pagamento ? ' - ' + m.pagamento : ''));
            $('#info-entrega').text(m.entrega || '-');
            $('#info-observacao').text(m.observacao || 'Sem observações.');

            $('#bloco-venda, #card-observacao').removeClass('escondido');
            $('#bloco-manual, #bloco-itens').addClass('escondido');
            $('#titulo-tabela-itens').text('Itens da venda');
            $btnVerDetalhes.text('Ver detalhes');
        } else {
            const ehSaida = m.tipo === 'Saída';
            $('#painel-titulo').text(ehSaida ? 'Informações da saída' : 'Informações da entrada');
            $('#info-manual-data').text(dataBR(m.data));
            $('#info-manual-descricao').text(m.descricao || (ehSaida ? '-' : 'Sem descrição.'));

            $('#bloco-venda, #card-observacao').addClass('escondido');
            $('#bloco-manual, #bloco-itens').removeClass('escondido');
            $('#titulo-tabela-itens').text(ehSaida ? 'Itens da saída' : 'Itens da entrada');
        }

        movSelecionadaId = m.id;
        $painel.addClass('ativo');
        $('.conteudo').addClass('painel-aberto');
        renderizarTabela();
    }

    function fecharPainel() {
        movSelecionadaId = null;
        $painel.removeClass('ativo');
        $('.conteudo').removeClass('painel-aberto');
        renderizarTabela();
    }

    $('#btn-fechar-painel').on('click', fecharPainel);

    $btnVerDetalhes.on('click', function () {
        const $itens = $('#bloco-itens');
        const estavaEscondido = $itens.hasClass('escondido');
        $itens.toggleClass('escondido', !estavaEscondido);
        $(this).text(estavaEscondido ? 'Ocultar detalhes' : 'Ver detalhes');
    });

    $('#btn-editar').on('click', function () {
        const m = todas().find(x => x.id === movSelecionadaId);
        if (!m) return;
        if (m.tipo === 'Venda') {
            alert('Vendas são geradas automaticamente pelas encomendas. Para alterar, edite a encomenda.');
            return;
        }
        abrirModal(m);
    });

    $('#btn-excluir').on('click', async function () {
        const m = todas().find(x => x.id === movSelecionadaId);
        if (!m) return;
        if (m.tipo === 'Venda') {
            alert('Vendas são geradas automaticamente pelas encomendas e não podem ser excluídas aqui.');
            return;
        }
        if (!confirm('Deseja realmente excluir esta movimentação?')) return;

        await Dados.excluirMovimentacao(m.id);
        fecharPainel();
        carregar();
    });

    /* ===================== MODAL NOVA / EDITAR ===================== */
    const $modal = $('#modal-movi');

    function preencherListaProdutos() {
        $('#lista-produtos').empty();
        catalogo.forEach(p => $('#lista-produtos').append(`<option value="${esc(p.nome)}"></option>`));
    }

    function limparErros() {
        $modal.find('.erro').text('');
        $modal.find('.invalido').removeClass('invalido');
    }

    function marcarErro($campo, idErro, mensagem) {
        $campo.addClass('invalido');
        $(idErro).text(mensagem);
    }

    function resetarCamposItem() {
        $('#item-produto').val('');
        $('#item-qtd').val('');
        $('#dica-preco').text('');
        $('#btn-add-item').text('Adicionar');
        itemEmEdicao = null;
    }

    function atualizarRotuloDescricao() {
        const saida = $('#mov-tipo').val() === 'Saída';
        $('#rotulo-descricao').text(saida ? 'Descrição (obrigatória)' : 'Descrição (opcional)');
    }

    const ICONE_EDITAR = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.1 2.1 0 0 1 3 3L12 15l-4 1 1-4z"/></svg>';
    const ICONE_EXCLUIR = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="M19 6l-1 14H6L5 6"/></svg>';

    function renderizarItensModal() {
        const $corpo = $('#corpo-itens-modal').empty();

        if (!itensModal.length) {
            $corpo.append('<tr><td colspan="5" class="vazio">Nenhum item adicionado.</td></tr>');
        }

        itensModal.forEach((i, idx) => {
            $corpo.append(`
                <tr>
                    <td>${esc(i.produto)}</td>
                    <td>${i.qtd}</td>
                    <td>${moeda(i.preco)}</td>
                    <td>${moeda(i.total)}</td>
                    <td class="acoes">
                        <button type="button" class="btn-icone" data-acao="editar" data-idx="${idx}" aria-label="Editar item">${ICONE_EDITAR}</button>
                        <button type="button" class="btn-icone" data-acao="excluir" data-idx="${idx}" aria-label="Excluir item">${ICONE_EXCLUIR}</button>
                    </td>
                </tr>`);
        });

        $('#mov-valor').val(moeda(somaItens(itensModal)));
    }

    function abrirModal(m) {
        limparErros();
        resetarCamposItem();

        if (m) {
            movEmEdicaoId = m.id;
            $('#modal-movi-titulo').text('Editar Movimentação');
            itensModal = m.itens.map(i => ({ ...i }));
            $('#mov-tipo').val(m.tipo);
            $('#mov-data').val(m.data);
            $('#mov-descricao').val(m.descricao);
        } else {
            movEmEdicaoId = null;
            $('#modal-movi-titulo').text('Nova Movimentação');
            itensModal = [];
            $('#mov-tipo').val('');
            $('#mov-data').val(hojeISO());
            $('#mov-descricao').val('');
        }

        atualizarRotuloDescricao();
        renderizarItensModal();
        $modal.addClass('aberto').attr('aria-hidden', 'false');
        $('#item-produto').trigger('focus');
    }

    function fecharModal() {
        $modal.removeClass('aberto').attr('aria-hidden', 'true');
        movEmEdicaoId = null;
        itensModal = [];
        resetarCamposItem();
        limparErros();
    }

    $('#btn-novo-movi').on('click', () => abrirModal());
    $('#btn-cancelar-movi').on('click', fecharModal);
    $modal.on('mousedown', function (e) { if (e.target === this) fecharModal(); });
    $(document).on('keydown', function (e) {
        if (e.key !== 'Escape') return;
        if ($modal.hasClass('aberto')) fecharModal();
        else if ($painel.hasClass('ativo')) fecharPainel();
    });

    $('#mov-tipo').on('change', function () {
        atualizarRotuloDescricao();
        $(this).removeClass('invalido');
        $('#erro-tipo').text('');
    });

    // Mostra preço e estoque ao escolher o produto
    $('#item-produto').on('input', function () {
        const p = buscarProduto($(this).val());
        $('#dica-preco').text(p ? 'Preço unitário: ' + moeda(p.preco) + ' · Estoque: ' + p.quantidade + ' un' : '');
        $('#erro-item').text('');
    });

    /* ----- itens do modal ----- */
    function adicionarItem() {
        const $erro = $('#erro-item').text('');
        const produto = buscarProduto($('#item-produto').val());
        const qtd = Number($('#item-qtd').val());

        if (!produto) { $erro.text('Escolha um produto cadastrado da lista.'); return; }
        if (!Number.isInteger(qtd) || qtd < 1) { $erro.text('Informe uma quantidade inteira maior que zero.'); return; }

        const novo = {
            produtoId: produto.id, produto: produto.nome, qtd: qtd,
            preco: produto.preco, total: arredonda(qtd * produto.preco)
        };

        if (itemEmEdicao !== null) {
            itensModal[itemEmEdicao] = novo;
        } else {
            const existente = itensModal.find(i => i.produtoId === novo.produtoId);
            if (existente) {
                existente.qtd += qtd;
                existente.total = arredonda(existente.qtd * existente.preco);
            } else {
                itensModal.push(novo);
            }
        }

        $('#erro-itens').text('');
        resetarCamposItem();
        renderizarItensModal();
        $('#item-produto').trigger('focus');
    }

    $('#btn-add-item').on('click', adicionarItem);
    $('#item-produto, #item-qtd').on('keydown', function (e) {
        if (e.key === 'Enter') { e.preventDefault(); adicionarItem(); }
    });

    $('#corpo-itens-modal').on('click', '.btn-icone', function () {
        const idx = Number($(this).data('idx'));

        if ($(this).data('acao') === 'editar') {
            const i = itensModal[idx];
            $('#item-produto').val(i.produto).trigger('input');
            $('#item-qtd').val(i.qtd);
            $('#btn-add-item').text('Salvar item');
            itemEmEdicao = idx;
            $('#item-qtd').trigger('focus');
        } else {
            itensModal.splice(idx, 1);
            if (itemEmEdicao !== null) resetarCamposItem();
            renderizarItensModal();
        }
    });

    /* ----- validação e salvar ----- */
    $('#form-movi').on('submit', async function (e) {
        e.preventDefault();
        limparErros();
        let valido = true;

        const tipo = $('#mov-tipo').val();
        const data = $('#mov-data').val();
        const descricao = $('#mov-descricao').val().trim();

        if (!itensModal.length) { $('#erro-itens').text('Adicione pelo menos um item.'); valido = false; }
        if (!tipo) { marcarErro($('#mov-tipo'), '#erro-tipo', 'Selecione o tipo.'); valido = false; }
        if (!data) { marcarErro($('#mov-data'), '#erro-data', 'Informe a data.'); valido = false; }
        if (tipo === 'Saída' && descricao.length < 3) {
            marcarErro($('#mov-descricao'), '#erro-descricao', 'Descreva o motivo da saída.');
            valido = false;
        }
        if (!valido) return;

        const dados = { tipo: tipo, data: data, descricao: descricao, itens: itensModal.map(i => ({ ...i })) };
        if (movEmEdicaoId !== null) dados.id = movEmEdicaoId;

        const salva = await Dados.salvarMovimentacao(dados);

        const idAberto = movSelecionadaId;
        fecharModal();
        await carregar();

        // Se o painel estava aberto na movimentação editada, atualiza
        if (idAberto === salva.id) abrirPainel(salva);
    });

    /* ===================== INICIALIZAÇÃO ===================== */
    $('.card-entradas h2').text(VENDAS_SOMENTE_DO_MES ? 'Vendas do mês' : 'Total em vendas');
    $('.card-saidas h2').text('Perdas e insumos do mês');
    $('.card-saldo h2').text('Saldo Atual');

    carregar();
});
