/* SnapSales - tela de Clientes (interface: tabela, busca, modal e validações) */
$(function () {

    const esc = Dados.esc;

    let clientes = [];
    let selecionadoId = null;   // linha selecionada na tabela
    let emEdicaoId = null;      // null = novo cliente | número = editando

    /* ===================== TABELA ===================== */
    async function carregar() {
        clientes = await Dados.listarClientes();
        renderizarTabela();
    }

    function filtrar() {
        const termo = $('.filtro-cliente input').val().toLowerCase().trim();
        return clientes.filter(c =>
            c.nome.toLowerCase().includes(termo) ||
            c.codigo.toLowerCase().includes(termo) ||
            c.telefone.includes(termo) ||
            c.endereco.toLowerCase().includes(termo)
        );
    }

    function renderizarTabela() {
        const lista = filtrar();
        const $tbody = $('#tabela-cliente-corpo').empty();

        if (!lista.length) {
            $tbody.append('<tr><td colspan="4">Nenhum cliente encontrado.</td></tr>');
            return;
        }

        lista.forEach(c => {
            const classe = c.id === selecionadoId ? ' class="linha-selecionada"' : '';
            $tbody.append(`
                <tr data-id="${c.id}"${classe}>
                    <td>${esc(c.codigo)}</td>
                    <td>${esc(c.nome)}</td>
                    <td>${esc(c.telefone)}</td>
                    <td>${esc(c.endereco)}</td>
                </tr>`);
        });
    }

    $('.filtro-cliente input').on('input', function () {
        selecionadoId = null;   // evita agir em um item que sumiu da lista
        renderizarTabela();
    });

    // Clicar numa linha seleciona; clicar de novo desmarca
    $('#tabela-cliente-corpo').on('click', 'tr', function () {
        const id = $(this).data('id');
        if (!id) return;
        selecionadoId = (selecionadoId === id) ? null : id;
        renderizarTabela();
    });

    function clienteSelecionado(mensagem) {
        if (!selecionadoId) {
            alert(mensagem);
            return null;
        }
        return clientes.find(c => c.id === selecionadoId);
    }

    /* ===================== BOTÕES DE AÇÃO ===================== */
    $('#btn-conversa').on('click', function () {
        const c = clienteSelecionado('Selecione um cliente na tabela para abrir a conversa.');
        if (!c) return;
        const numero = c.telefone.replace(/\D/g, '');
        window.open('https://web.whatsapp.com/send?phone=55' + numero, '_blank');
    });

    $('#btn-editar').on('click', function () {
        const c = clienteSelecionado('Selecione um cliente para editar.');
        if (c) abrirModal(c);
    });

    $('#btn-excluir').on('click', async function () {
        const c = clienteSelecionado('Selecione um cliente para excluir.');
        if (!c) return;
        if (!confirm('Deseja realmente excluir este cliente?')) return;

        await Dados.excluirCliente(c.id);
        selecionadoId = null;
        carregar();
    });

    /* ===================== MODAL NOVO / EDITAR ===================== */
    const $modal = $('#modal-cliente');
    const $form = $('#form-cliente');
    const $nome = $('#cliente-nome');
    const $telefone = $('#cliente-telefone');
    const $endereco = $('#cliente-endereco');

    function limparErros() {
        $form.find('input').removeClass('invalido');
        $form.find('.erro').text('');
    }

    function abrirModal(cliente) {
        limparErros();

        if (cliente) {
            emEdicaoId = cliente.id;
            $('#modal-titulo').text('Editar Cliente');
            $nome.val(cliente.nome);
            $telefone.val(cliente.telefone);
            $endereco.val(cliente.endereco);
        } else {
            emEdicaoId = null;
            $('#modal-titulo').text('Novo Cliente');
            $form[0].reset();
        }

        $modal.addClass('aberto').attr('aria-hidden', 'false');
        $nome.trigger('focus');
    }

    function fecharModal() {
        $modal.removeClass('aberto').attr('aria-hidden', 'true');
        emEdicaoId = null;
        $form[0].reset();
        limparErros();
    }

    $('#btn-novo').on('click', () => abrirModal());
    $('#modal-fechar, #btn-cancelar').on('click', fecharModal);

    // Fecha ao clicar no fundo escuro ou com ESC
    $modal.on('mousedown', function (e) { if (e.target === this) fecharModal(); });
    $(document).on('keydown', function (e) {
        if (e.key === 'Escape' && $modal.hasClass('aberto')) fecharModal();
    });

    // Máscara do telefone: "51 999999999"
    $telefone.on('input', function () {
        let d = $(this).val().replace(/\D/g, '').slice(0, 11);
        if (d.length > 2) d = d.slice(0, 2) + ' ' + d.slice(2);
        $(this).val(d);
    });

    // Tira o destaque de erro quando a pessoa volta a digitar
    $form.find('input').on('input', function () {
        $(this).removeClass('invalido').next('.erro').text('');
    });

    function mostrarErro($campo, idErro, mensagem) {
        $campo.addClass('invalido');
        $(idErro).text(mensagem);
    }

    function validar() {
        limparErros();
        let valido = true;

        const nome = $nome.val().trim();
        const digitos = $telefone.val().replace(/\D/g, '');
        const endereco = $endereco.val().trim();

        if (nome.length < 3) {
            mostrarErro($nome, '#erro-nome', 'Informe o nome com pelo menos 3 letras.');
            valido = false;
        }
        if (digitos.length < 10 || digitos.length > 11) {
            mostrarErro($telefone, '#erro-telefone', 'Informe o DDD e o número. Ex.: 51 999999999');
            valido = false;
        }
        if (endereco.length < 5) {
            mostrarErro($endereco, '#erro-endereco', 'Informe o endereço com pelo menos 5 caracteres.');
            valido = false;
        }

        if (!valido) $form.find('.invalido').first().trigger('focus');
        return valido;
    }

    $form.on('submit', async function (e) {
        e.preventDefault();
        if (!validar()) return;

        const dados = {
            nome: $nome.val().trim(),
            telefone: $telefone.val().trim(),
            endereco: $endereco.val().trim()
        };
        if (emEdicaoId !== null) dados.id = emEdicaoId;

        await Dados.salvarCliente(dados);   // se o servidor recusar, mostra o erro e mantém o modal aberto

        fecharModal();
        $('.filtro-cliente input').val('');
        selecionadoId = null;
        carregar();
    });

    carregar();
});
