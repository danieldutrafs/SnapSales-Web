/* SnapSales - tela de Produtos e Estoque (interface: cards, tabela, busca, modal e validações) */
$(function () {

    const esc = Dados.esc;
    const fmt = n => Number(n).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const nomeCategoria = p => (p.categoria && typeof p.categoria === 'object') ? p.categoria.nome : p.categoria;

    let produtos = [];
    let selecionadoId = null;

    /* ===================== TELA ===================== */
    async function carregar() {
        produtos = await Dados.listarProdutos();
        atualizarTela();
    }

    function atualizarTela() {
        renderizarTabela(filtrar());
        atualizarCards();
    }

    // Os cards mostram o total do estoque inteiro (não só o que está filtrado)
    function atualizarCards() {
        let qtdTotal = 0, valorTotal = 0, ativos = 0;

        produtos.forEach(p => {
            qtdTotal += p.quantidade;
            valorTotal += p.quantidade * p.precoUni;
            if (p.ativo) ativos++;
        });

        $('#quantidade-total').text(qtdTotal.toLocaleString('pt-BR') + ' uni');
        $('#valor-total').text('R$ ' + fmt(valorTotal));
        $('#produtos-ativos').text(ativos + ' Itens');
    }

    function filtrar() {
        const termo = ($('.filtro-produto input').val() || '').toLowerCase().trim();
        return produtos.filter(p =>
            String(p.codigo).toLowerCase().includes(termo) ||
            String(p.nome).toLowerCase().includes(termo) ||
            String(nomeCategoria(p)).toLowerCase().includes(termo)
        );
    }

    function renderizarTabela(lista) {
        const $tbody = $('#tabela-produtos-corpo').empty();

        if (!lista.length) {
            $tbody.append('<tr><td colspan="6">Nenhum produto encontrado.</td></tr>');
            return;
        }

        lista.forEach(p => {
            const classes = (p.ativo ? '' : 'linha-pausada ') + (p.id === selecionadoId ? 'linha-selecionada' : '');
            $tbody.append(`
                <tr data-id="${p.id}" class="${classes}">
                    <td>${esc(p.codigo)}</td>
                    <td>${esc(p.nome)}</td>
                    <td>${esc(nomeCategoria(p))}</td>
                    <td>${p.quantidade}</td>
                    <td>R$ ${fmt(p.precoUni)}</td>
                    <td>R$ ${fmt(p.quantidade * p.precoUni)}</td>
                </tr>`);
        });
    }

    $('.filtro-produto input').on('input', function () {
        selecionadoId = null;   // evita agir em um item que sumiu da lista
        atualizarTela();
    });

    // Clicar numa linha seleciona; clicar de novo desmarca
    $('#tabela-produtos-corpo').on('click', 'tr', function () {
        const id = $(this).data('id');
        if (!id) return;
        selecionadoId = (selecionadoId === id) ? null : id;
        atualizarTela();
    });

    function produtoSelecionado(mensagem) {
        if (!selecionadoId) {
            alert(mensagem);
            return null;
        }
        return produtos.find(p => p.id === selecionadoId);
    }

    /* ===================== PAUSAR / EXCLUIR ===================== */
    $('#btn-pausar').on('click', async function () {
        const p = produtoSelecionado('Selecione um produto na tabela primeiro.');
        if (!p) return;
        await Dados.alternarAtivoProduto(p.id);
        carregar();
    });

    $('#btn-excluir').on('click', async function () {
        const p = produtoSelecionado('Selecione um produto na tabela primeiro.');
        if (!p) return;
        if (!confirm('Deseja realmente excluir este produto?')) return;

        await Dados.excluirProduto(p.id);
        selecionadoId = null;
        carregar();
    });

    /* ===================== MODAL ===================== */
    async function carregarSelectCategorias() {
        const $select = $('#campo-categoria-produto').empty();
        $select.append('<option value="">Selecione uma categoria...</option>');
        (await Dados.listarCategorias()).forEach(c => $select.append(`<option value="${c.id}">${esc(c.nome)}</option>`));
        $select.append('<option value="__NOVA__">+ Adicionar Nova Categoria...</option>');
    }

    async function abrirModal(titulo, produto) {
        $('#modal-titulo-produto').text(titulo);
        $('#formulario-produto')[0].reset();
        $('#grupo-nova-categoria').addClass('oculto');
        await carregarSelectCategorias();

        if (produto) {
            $('#campo-id-produto').val(produto.id);
            $('#campo-nome-produto').val(produto.nome);
            $('#campo-categoria-produto').val(produto.categoria.id);
            $('#campo-preco-produto').val(produto.precoUni);
            $('#campo-quantidade-produto').val(produto.quantidade);
        } else {
            $('#campo-id-produto').val('');
        }

        $('#fundo-modal-produto').removeClass('oculto');
        $('#campo-nome-produto').trigger('focus');
    }

    function fecharModal() {
        $('#fundo-modal-produto').addClass('oculto');
    }

    $('#btn-novo-produto').on('click', () => abrirModal('Novo Produto'));

    $('#btn-editar').on('click', function () {
        const p = produtoSelecionado('Selecione um produto na tabela para editar.');
        if (p) abrirModal('Editar Produto', p);
    });

    $('#btn-fechar-modal-produto, #btn-cancelar-modal-produto').on('click', fecharModal);
    $('#fundo-modal-produto').on('mousedown', function (e) { if (e.target === this) fecharModal(); });
    $(document).on('keydown', function (e) { if (e.key === 'Escape') fecharModal(); });

    // Escolher "+ Adicionar Nova Categoria..." mostra o campo de nome
    $('#campo-categoria-produto').on('change', function () {
        const nova = $(this).val() === '__NOVA__';
        $('#grupo-nova-categoria').toggleClass('oculto', !nova);
        if (nova) $('#campo-nova-categoria').trigger('focus');
    });

    /* ===================== VALIDAÇÃO E SALVAR ===================== */
    $('#formulario-produto').on('submit', async function (e) {
        e.preventDefault();

        const idProduto = $('#campo-id-produto').val();
        const nome = $('#campo-nome-produto').val().trim();
        const valCategoria = $('#campo-categoria-produto').val();
        const precoUni = parseFloat($('#campo-preco-produto').val());
        const quantidade = parseInt($('#campo-quantidade-produto').val(), 10);
        const nomeNovaCategoria = $('#campo-nova-categoria').val().trim();

        if (nome.length < 2) { alert('Informe o nome do produto.'); return; }
        if (!valCategoria) { alert('Selecione uma categoria.'); return; }
        if (valCategoria === '__NOVA__' && !nomeNovaCategoria) { alert('Informe o nome da nova categoria.'); return; }
        if (isNaN(precoUni) || precoUni < 0) { alert('Informe um preço unitário válido.'); return; }
        if (isNaN(quantidade) || quantidade < 0) { alert('Informe uma quantidade válida (0 ou mais).'); return; }

        const duplicado = produtos.some(p =>
            p.nome.toLowerCase() === nome.toLowerCase() && String(p.id) !== String(idProduto));
        if (duplicado) { alert('Já existe um produto com esse nome.'); return; }

        const categoria = valCategoria === '__NOVA__'
            ? await Dados.obterOuCriarCategoria(nomeNovaCategoria)
            : { id: parseInt(valCategoria, 10) };

        const dados = { nome: nome, categoria: categoria, precoUni: precoUni, quantidade: quantidade };
        if (idProduto) dados.id = parseInt(idProduto, 10);

        await Dados.salvarProduto(dados);

        fecharModal();
        carregar();
    });

    carregar();
});
