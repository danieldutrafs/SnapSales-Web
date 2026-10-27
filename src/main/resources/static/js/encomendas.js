/* SnapSales - tela de Encomendas (interface: cards, filtros, painel de detalhes, modal e validações) */
$(function () {

    const moeda = Dados.moeda;
    const esc = Dados.esc;
    const arredonda = n => Math.round(n * 100) / 100;

    let encomendas = [];
    let filtroAtual = 'todos';
    let idPainel = null;   // encomenda aberta no painel direito

    async function carregarEncomendas() {
        encomendas = (await Dados.listarEncomendas()).map(Dados.formatarEncomenda);
    }

    /* ===================== CARDS ===================== */
    function renderizarCards(lista) {
        const $grid = $('#lista-encomendas').empty();

        if (!lista.length) {
            $grid.append('<p class="vazio">Nenhuma encomenda encontrada.</p>');
            return;
        }

        lista.forEach(function (enc) {
            $grid.append(`
                <div class="card-encomenda">
                    <div class="card-encomenda-header">
                        <div>
                            <h3>Código</h3>
                            <p>${esc(enc.codigo)}</p>
                        </div>
                        <span class="badge-status ${enc.status}">${esc(enc.statusTexto)}</span>
                    </div>
                    <div class="card-info-grupo">
                        <label>Cliente</label>
                        <span>${esc(enc.cliente)}</span>
                    </div>
                    <div class="card-info-grupo">
                        <label>Data Entrega</label>
                        <span>${esc(enc.dataEntrega)}</span>
                    </div>
                    <div class="card-info-grupo">
                        <label>Valor Total</label>
                        <span>${esc(enc.valorTotal)}</span>
                    </div>
                    <button class="btn-detalhes" data-id="${enc.id}">Ver detalhes</button>
                </div>`);
        });
    }

    function aplicarFiltro() {
        renderizarCards(filtroAtual === 'todos'
            ? encomendas
            : encomendas.filter(e => e.status === filtroAtual));
    }

    /* ===================== FILTROS (indicador deslizante) ===================== */
    const $indicadorFiltro = $('#filtro-indicador');
    const TRANSICAO = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';

    function moverIndicadorFiltro($botao, animado) {
        if (!$botao.length) return;
        $indicadorFiltro.css('transition', animado ? TRANSICAO : 'none');
        $indicadorFiltro.css({
            transform: 'translateX(' + $botao.position().left + 'px)',
            width: $botao.outerWidth() + 'px',
            opacity: '1'
        });
    }

    const $filtroInicial = $('.filtros button.ativo');
    moverIndicadorFiltro($filtroInicial, false);
    setTimeout(function () { $indicadorFiltro.css('transition', TRANSICAO); }, 50);

    $('.filtros button').on('click', function () {
        $('.filtros button').removeClass('ativo');
        $(this).addClass('ativo');
        moverIndicadorFiltro($(this), true);

        filtroAtual = $(this).data('status');
        aplicarFiltro();
    });

    /* ===================== PAINEL DE DETALHES ===================== */
    function abrirPainel(id) {
        const enc = encomendas.find(e => e.id === id);
        if (!enc) return;
        idPainel = id;

        $('#detalhe-codigo').text(enc.codigo);
        $('#detalhe-nome-cliente').text(enc.cliente);
        $('#detalhe-telefone-cliente').text(enc.telefone);

        const $lista = $('#detalhe-lista-itens').empty();
        enc.itens.forEach(item => $lista.append('<li>' + esc(item) + '</li>'));

        $('#detalhe-total-itens').text('Total: ' + enc.totalItens);
        $('#detalhe-endereco').text(enc.endereco);
        $('#detalhe-destinatario').text(enc.destinatario).toggleClass('escondido', !enc.destinatario);
        $('#detalhe-data-hora').text(enc.dataEntrega);

        $('#detalhe-descricao').text(enc.descricao);
        $('#card-descricao').toggleClass('escondido', !enc.descricao);

        $('#detalhe-frete').text(enc.frete);
        $('#detalhe-subtotal-itens').text(enc.subtotal);
        $('#detalhe-entrada').text(enc.entrada);
        $('#detalhe-a-pagar').text(enc.aPagar);

        // preparação -> pronto -> entregue
        $('#btn-pronto').toggleClass('escondido', enc.status !== 'preparacao');
        $('#btn-entregue').toggleClass('escondido', enc.status === 'entregue');

        $('#painel-direito').addClass('aberto');
        $('.conteudo').addClass('painel-aberto');
    }

    function fecharPainel() {
        idPainel = null;
        $('#painel-direito').removeClass('aberto');
        $('.conteudo').removeClass('painel-aberto');
    }

    async function atualizarLista() {
        await carregarEncomendas();
        aplicarFiltro();
    }

    $(document).on('click', '.btn-detalhes', function () { abrirPainel($(this).data('id')); });
    $('#btn-fechar').on('click', fecharPainel);

    $('#btn-pronto, #btn-entregue').on('click', async function () {
        const novoStatus = $(this).data('status');
        if (novoStatus === 'entregue' &&
            !confirm('Marcar como entregue? Depois disso a encomenda não poderá mais ser editada.')) return;

        await Dados.alterarStatus(idPainel, novoStatus);
        await atualizarLista();
        abrirPainel(idPainel);
    });

    $('#btn-excluir').on('click', async function () {
        if (!idPainel || !confirm('Deseja realmente excluir esta encomenda?')) return;

        await Dados.excluirEncomenda(idPainel);
        fecharPainel();
        await atualizarLista();
    });

    $('#btn-editar').on('click', async function () {
        const enc = await Dados.buscarEncomenda(idPainel);
        if (!enc) return;
        if (enc.status === 'entregue') {
            alert('Encomendas entregues não podem ser editadas.');
            return;
        }
        abrirModal(enc);
    });

    /* ===================== MODAL: NOVA / EDITAR ===================== */
    const $modal = $('#modal-encomenda');
    let clientes = [];
    let catalogo = [];         // produtos (com estoque) vindos do servidor
    let itens = [];            // { produtoId, produto, qtd, preco, total }
    let encEmEdicao = null;    // null = nova | objeto = editando

    const subtotal = () => arredonda(itens.reduce((s, i) => s + i.total, 0));

    function numero(seletor) {
        const v = parseFloat($(seletor).val());
        return isNaN(v) ? 0 : v;
    }

    function limparErros() {
        $modal.find('.erro').text('');
        $modal.find('.invalido').removeClass('invalido');
    }

    function marcarErro($campo, idErro, mensagem) {
        $campo.addClass('invalido');
        $(idErro).text(mensagem);
    }

    // Retirada no local = sem frete: o campo fica bloqueado e zerado
    function atualizarFretePorRetirada() {
        const retirada = $('#enc-retirada').is(':checked');
        $('#enc-frete').prop('disabled', retirada);
        if (retirada) {
            $('#enc-frete').val('').removeClass('invalido');
            $('#erro-frete').text('');
        }
        atualizarResumo();
    }

    // Quanto ainda dá para adicionar na lista: estoque do produto - o que já está nos itens
    function estoqueDisponivel(p) {
        let disp = p.quantidade;
        itens.forEach(i => { if (i.produtoId === p.id) disp -= i.qtd; });
        return disp;
    }

    async function carregarClientes() {
        clientes = await Dados.listarClientes();
        const $select = $('#enc-cliente').empty().append('<option value="">Selecione o cliente</option>');
        clientes.forEach(c => $select.append(`<option value="${c.id}">${esc(c.codigo + ' - ' + c.nome)}</option>`));
    }

    // Só aparecem produtos ativos e com estoque
    function carregarProdutos() {
        const $select = $('#item-produto');
        const atual = $select.val();
        const disponiveis = catalogo.filter(p => p.ativo && estoqueDisponivel(p) > 0);

        $select.empty().append(`<option value="">${disponiveis.length ? 'Selecione o produto' : 'Nenhum produto com estoque'}</option>`);
        disponiveis.forEach(p => {
            $select.append(`<option value="${p.id}">${esc(p.nome)} - ${moeda(p.preco)} (estoque: ${estoqueDisponivel(p)})</option>`);
        });

        if (atual && $select.find(`option[value="${atual}"]`).length) $select.val(atual);
    }

    function renderizarItens() {
        const $corpo = $('#corpo-itens').empty();

        if (!itens.length) {
            $corpo.append('<tr><td colspan="4" class="vazio">Nenhum item adicionado.</td></tr>');
        }

        itens.forEach((i, idx) => {
            $corpo.append(`
                <tr>
                    <td>${esc(i.produto)}</td>
                    <td>${i.qtd}</td>
                    <td>${moeda(i.total)}</td>
                    <td class="acao"><button type="button" class="btn-remover" data-idx="${idx}">Remover</button></td>
                </tr>`);
        });

        $('#total-itens-modal').text('Total: ' + moeda(subtotal()));
        carregarProdutos();
        atualizarResumo();
    }

    function atualizarResumo() {
        const sub = subtotal();
        const frete = Math.max(numero('#enc-frete'), 0);
        const entrada = Math.max(numero('#enc-entrada'), 0);

        $('#res-itens').text('Itens: ' + moeda(sub));
        $('#res-frete').text('Frete: ' + moeda(frete));
        $('#res-entrada').text('Entrada: ' + (entrada > 0 ? '-' : '') + moeda(entrada));
        $('#res-apagar').text('A pagar: ' + moeda(Math.max(arredonda(sub + frete - entrada), 0)));
    }

    async function abrirModal(enc) {
        limparErros();
        encEmEdicao = enc || null;
        itens = enc
            ? enc.itens.map(i => ({
                produtoId: i.produtoId, produto: i.produto, qtd: i.qtd, preco: i.preco,
                total: arredonda(i.qtd * i.preco)
            }))
            : [];

        await carregarClientes();
        catalogo = await Dados.catalogo();
        // Ao editar, o estoque do servidor já descontou os itens desta encomenda: soma de volta
        if (enc) {
            enc.itens.forEach(i => {
                const p = catalogo.find(x => x.id === i.produtoId);
                if (p) p.quantidade += i.qtd;
            });
        }
        $('#form-encomenda')[0].reset();
        $('#modal-enc-titulo').text(enc ? 'Editar Encomenda' : 'Nova Encomenda');

        const status = enc ? enc.status : 'preparacao';
        $('#modal-enc-badge').attr('class', 'badge-status ' + status).text(Dados.STATUS_TEXTO[status]);

        if (enc) {
            // Se o cliente foi excluído depois, mantém ele na lista para poder salvar
            if (!clientes.some(c => c.id === enc.clienteId)) {
                clientes.push({ id: enc.clienteId, codigo: '', nome: enc.cliente, telefone: enc.telefone, endereco: enc.endereco || '' });
                $('#enc-cliente').append(`<option value="${enc.clienteId}">${esc(enc.cliente)}</option>`);
            }
            $('#enc-cliente').val(String(enc.clienteId));
            $('#enc-retirada').prop('checked', !!enc.retirada);
            $('#enc-endereco').val(enc.endereco || '');
            $('#enc-destinatario').val(enc.destinatario || '');
            $('#enc-data').removeAttr('min').val(enc.dataEntrega);
            $('#enc-hora').val(enc.horaEntrega);
            $('#enc-descricao').val(enc.observacao || '');
            $('#enc-frete').val(enc.frete || '');
            $('#enc-entrada').val(enc.entrada || '');
            $('#enc-pagamento').val(enc.pagamento);
        } else {
            $('#enc-data').attr('min', Dados.hojeISO()).val(Dados.hojeISO());
            $('#enc-hora').val('12:00');
        }

        $('#campo-endereco').toggleClass('escondido', $('#enc-retirada').is(':checked'));
        atualizarFretePorRetirada();
        renderizarItens();

        $modal.addClass('aberto').attr('aria-hidden', 'false');
        $('#enc-cliente').trigger('focus');
    }

    function fecharModal() {
        $modal.removeClass('aberto').attr('aria-hidden', 'true');
        encEmEdicao = null;
        limparErros();
    }

    $('#btn-nova-encomenda').on('click', () => abrirModal());
    $('#btn-cancelar-enc').on('click', fecharModal);
    $modal.on('mousedown', function (e) { if (e.target === this) fecharModal(); });
    $(document).on('keydown', function (e) {
        if (e.key === 'Escape' && $modal.hasClass('aberto')) fecharModal();
    });

    // Ao escolher o cliente, sugere destinatário e endereço (se estiverem vazios)
    $('#enc-cliente').on('change', function () {
        $(this).removeClass('invalido');
        $('#erro-cliente').text('');
        const c = clientes.find(x => String(x.id) === $(this).val());
        if (!c) return;
        if (!$('#enc-destinatario').val().trim()) $('#enc-destinatario').val(c.nome);
        if (!$('#enc-retirada').is(':checked') && !$('#enc-endereco').val().trim()) $('#enc-endereco').val(c.endereco);
    });

    $('#enc-retirada').on('change', function () {
        $('#campo-endereco').toggleClass('escondido', this.checked);
        $('#enc-endereco').removeClass('invalido');
        $('#erro-endereco').text('');
        atualizarFretePorRetirada();
    });

    /* ----- itens da encomenda ----- */
    function adicionarItem() {
        $('#erro-item').text('');
        const produto = catalogo.find(p => String(p.id) === $('#item-produto').val());
        const qtd = Number($('#item-qtd').val());

        if (!produto) { $('#erro-item').text('Selecione um produto.'); return; }
        if (!Number.isInteger(qtd) || qtd < 1) { $('#erro-item').text('Informe uma quantidade inteira maior que zero.'); return; }

        const disp = estoqueDisponivel(produto);
        if (qtd > disp) { $('#erro-item').text('Estoque insuficiente. Disponível: ' + disp + ' un.'); return; }

        const existente = itens.find(i => i.produtoId === produto.id);
        if (existente) {
            existente.qtd += qtd;
            existente.total = arredonda(existente.qtd * existente.preco);
        } else {
            itens.push({
                produtoId: produto.id, produto: produto.nome, qtd: qtd,
                preco: produto.preco, total: arredonda(qtd * produto.preco)
            });
        }

        $('#erro-itens').text('');
        $('#item-produto').val('');
        $('#item-qtd').val('');
        renderizarItens();
        $('#item-produto').trigger('focus');
    }

    $('#btn-add-item').on('click', adicionarItem);
    $('#item-qtd').on('keydown', function (e) {
        if (e.key === 'Enter') { e.preventDefault(); adicionarItem(); }
    });

    $('#corpo-itens').on('click', '.btn-remover', function () {
        itens.splice(Number($(this).data('idx')), 1);
        renderizarItens();
    });

    $('#enc-frete, #enc-entrada').on('input', function () {
        $(this).removeClass('invalido');
        $('#erro-frete, #erro-entrada').text('');
        atualizarResumo();
    });

    $('#form-encomenda').find('input, textarea').on('input', function () {
        $(this).removeClass('invalido');
    });

    /* ----- validação e salvar ----- */
    $('#form-encomenda').on('submit', async function (e) {
        e.preventDefault();
        limparErros();
        let valido = true;

        const cliente = clientes.find(c => String(c.id) === $('#enc-cliente').val());
        const retirada = $('#enc-retirada').is(':checked');
        const endereco = $('#enc-endereco').val().trim();
        const destinatario = $('#enc-destinatario').val().trim();
        const dataEntrega = $('#enc-data').val();
        const horaEntrega = $('#enc-hora').val();
        const frete = retirada ? 0 : numero('#enc-frete');
        const entrada = numero('#enc-entrada');
        const sub = subtotal();

        if (!cliente) { marcarErro($('#enc-cliente'), '#erro-cliente', 'Selecione o cliente da encomenda.'); valido = false; }
        if (!itens.length) { $('#erro-itens').text('Adicione pelo menos um item.'); valido = false; }
        if (!retirada && endereco.length < 5) {
            marcarErro($('#enc-endereco'), '#erro-endereco', 'Informe o endereço de entrega ou marque retirada no local.');
            valido = false;
        }
        if (destinatario.length < 3) {
            marcarErro($('#enc-destinatario'), '#erro-destinatario', 'Informe o nome do destinatário.');
            valido = false;
        }
        if (!dataEntrega || !horaEntrega) {
            marcarErro($('#enc-data'), '#erro-data', 'Informe a data e a hora da entrega.');
            valido = false;
        } else if (dataEntrega < Dados.hojeISO() && !(encEmEdicao && dataEntrega === encEmEdicao.dataEntrega)) {
            marcarErro($('#enc-data'), '#erro-data', 'A data da entrega não pode ser anterior a hoje.');
            valido = false;
        }
        if (frete < 0) { marcarErro($('#enc-frete'), '#erro-frete', 'O frete não pode ser negativo.'); valido = false; }
        if (entrada < 0) {
            marcarErro($('#enc-entrada'), '#erro-entrada', 'A entrada não pode ser negativa.');
            valido = false;
        } else if (entrada > arredonda(sub + frete)) {
            marcarErro($('#enc-entrada'), '#erro-entrada', 'A entrada não pode ser maior que o total (' + moeda(sub + frete) + ').');
            valido = false;
        }

        if (!valido) {
            $modal.find('.invalido').first().trigger('focus');
            return;
        }

        const dados = {
            clienteId: cliente.id,
            cliente: cliente.nome,
            telefone: cliente.telefone,
            itens: itens.map(i => ({ produtoId: i.produtoId, produto: i.produto, qtd: i.qtd, preco: i.preco })),
            retirada: retirada,
            endereco: retirada ? '' : endereco,
            destinatario: destinatario,
            dataEntrega: dataEntrega,
            horaEntrega: horaEntrega,
            observacao: $('#enc-descricao').val().trim(),
            pagamento: $('#enc-pagamento').val(),
            frete: frete,
            entrada: entrada
        };

        const editando = encEmEdicao;
        if (editando) await Dados.atualizarEncomenda(editando.id, dados);
        else await Dados.adicionarEncomenda(dados);

        fecharModal();
        await atualizarLista();

        if (editando) {
            abrirPainel(editando.id);
        } else {
            // Volta para "Todos" para a nova encomenda aparecer
            $('.filtros button[data-status="todos"]').trigger('click');
        }
    });

    atualizarLista();
});
