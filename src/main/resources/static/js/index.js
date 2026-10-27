/* SnapSales - Home (resumo e próximas entregas) */
$(async function () {

    const esc = Dados.esc;
    const LIMITE_ESTOQUE_BAIXO = 10;

    const encomendas = await Dados.listarEncomendas();
    const produtos = await Dados.listarProdutos();
    const hoje = Dados.hojeISO();

    const abertas = encomendas.filter(e => e.status !== 'entregue');

    $('#res-preparacao').text(encomendas.filter(e => e.status === 'preparacao').length);
    $('#res-prontas').text(encomendas.filter(e => e.status === 'pronto').length);
    $('#res-hoje').text(abertas.filter(e => e.dataEntrega === hoje).length);
    $('#res-estoque-baixo').text(produtos.filter(p => p.ativo && p.quantidade <= LIMITE_ESTOQUE_BAIXO).length);

    // Encomendas ainda não entregues, da entrega mais próxima para a mais distante
    const proximas = abertas
        .slice()
        .sort((a, b) => (a.dataEntrega + a.horaEntrega).localeCompare(b.dataEntrega + b.horaEntrega))
        .slice(0, 6)
        .map(Dados.formatarEncomenda);

    const $tbody = $('#tabela-proximas').empty();

    if (!proximas.length) {
        $tbody.append('<tr><td colspan="5">Nenhuma entrega pendente.</td></tr>');
        return;
    }

    proximas.forEach(e => {
        $tbody.append(`
            <tr>
                <td>${esc(e.codigo)}</td>
                <td>${esc(e.cliente)}</td>
                <td>${esc(e.dataEntrega)}</td>
                <td><span class="badge-status ${e.status}">${esc(e.statusTexto)}</span></td>
                <td>${esc(e.valorTotal)}</td>
            </tr>`);
    });
});
