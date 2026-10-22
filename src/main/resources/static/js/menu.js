/* ==========================================================================
   SnapSales - menu.js
   Animação da pílula azul do menu lateral. Usado por todas as telas.
   ========================================================================== */
$(function () {
    const $indicador = $('#menu-indicador');
    const TRANSICAO = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';

    function mover($link, animado) {
        const $item = $link.closest('li');
        if (!$item.length) return;

        $indicador.css('transition', animado ? TRANSICAO : 'none');
        $indicador.css({
            transform: 'translateY(' + $item.position().top + 'px)',
            height: $item.outerHeight() + 'px',
            opacity: '1'
        });
    }

    const $ativo = $('.menu-esquerdo nav a.ativo');

    if ($ativo.length) {
        mover($ativo, false);
        setTimeout(function () { $indicador.css('transition', TRANSICAO); }, 50);
    }

    $('.menu-esquerdo nav a').on('mouseenter', function () {
        mover($(this), true);
    });

    $('.menu-esquerdo nav').on('mouseleave', function () {
        if ($ativo.length) mover($ativo, true);
    });
});
