-- SnapSales - dados de exemplo (OPCIONAL)
-- Use so se quiser o sistema ja com dados para testar as telas.
-- 1) Rode a aplicacao uma vez (ela cria o banco e as tabelas).
-- 2) Abra este arquivo no MySQL Workbench e execute tudo (raio).
-- Rode UMA vez so. Para repetir: DROP DATABASE snapsales; e inicie a aplicacao de novo.

USE snapsales;

INSERT INTO categoria (nome) VALUES ('Salgados'), ('Doces'), ('Bebidas'), ('Bolos e Tortas');

INSERT INTO cliente (nome, telefone, endereco) VALUES
  ('Maria Silva',   '51999991111', 'Rua das Flores, 120'),
  ('João Pereira',  '51988882222', 'Av. Brasil, 450'),
  ('Ana Souza',     '51977773333', 'Rua Central, 33'),
  ('Carlos Mendes', '51966664444', 'Travessa do Sol, 78'),
  ('Fernanda Lima', '51955555555', 'Rua XV de Novembro, 910');

-- (Os estoques ja consideram o que as encomendas ainda nao entregues reservaram.)
INSERT INTO produto (nome, quantidade, preco, ativo, categoria_id) VALUES
  ('Coxinha de Frango',    40, 1.50,  TRUE,  (SELECT id FROM categoria WHERE nome = 'Salgados')),
  ('Pastel de Carne',      20, 2.00,  TRUE,  (SELECT id FROM categoria WHERE nome = 'Salgados')),
  ('Brigadeiro',          160, 2.00,  TRUE,  (SELECT id FROM categoria WHERE nome = 'Doces')),
  ('Beijinho',            150, 2.00,  TRUE,  (SELECT id FROM categoria WHERE nome = 'Doces')),
  ('Refrigerante Lata',    24, 5.00,  TRUE,  (SELECT id FROM categoria WHERE nome = 'Bebidas')),
  ('Suco Natural 500ml',    0, 7.50,  TRUE,  (SELECT id FROM categoria WHERE nome = 'Bebidas')),
  ('Bolo de Cenoura',      10, 40.00, TRUE,  (SELECT id FROM categoria WHERE nome = 'Bolos e Tortas')),
  ('Torta de Limão',        4, 55.00, FALSE, (SELECT id FROM categoria WHERE nome = 'Bolos e Tortas'));

-- ---------- Encomendas (cada bloco: a encomenda + seus itens) ----------

INSERT INTO encomenda (cliente_id, status, retirada, endereco_entrega, destinatario, frete, data_entrega, hora_entrega, observacao, forma_pagamento, valor_entrada, valor_total, criado_em)
VALUES ((SELECT id FROM cliente WHERE nome = 'Maria Silva'), 'preparacao', FALSE, 'Rua das Flores, 120', 'Maria Silva', 10.00,
        CURDATE() + INTERVAL 2 DAY, '15:00', 'Festa de aniversário. Entregar no salão.', 'Pix', 50.00, 165.00, CURDATE());
SET @e = LAST_INSERT_ID();
INSERT INTO item_encomenda (encomenda_id, produto_id, quantidade, preco_momento) VALUES
  (@e, (SELECT id FROM produto WHERE nome = 'Coxinha de Frango'), 50, 1.50),
  (@e, (SELECT id FROM produto WHERE nome = 'Brigadeiro'),        40, 2.00);

INSERT INTO encomenda (cliente_id, status, retirada, endereco_entrega, destinatario, frete, data_entrega, hora_entrega, observacao, forma_pagamento, valor_entrada, valor_total, criado_em)
VALUES ((SELECT id FROM cliente WHERE nome = 'João Pereira'), 'pronto', TRUE, NULL, 'João Pereira', 0.00,
        CURDATE() + INTERVAL 1 DAY, '18:30', '', 'Dinheiro', 0.00, 120.00, CURDATE() - INTERVAL 1 DAY);
SET @e = LAST_INSERT_ID();
INSERT INTO item_encomenda (encomenda_id, produto_id, quantidade, preco_momento) VALUES
  (@e, (SELECT id FROM produto WHERE nome = 'Refrigerante Lata'), 24, 5.00);

INSERT INTO encomenda (cliente_id, status, retirada, endereco_entrega, destinatario, frete, data_entrega, hora_entrega, observacao, forma_pagamento, valor_entrada, valor_total, criado_em)
VALUES ((SELECT id FROM cliente WHERE nome = 'Ana Souza'), 'entregue', FALSE, 'Rua Central, 33', 'Pedro Souza', 8.00,
        CURDATE() - INTERVAL 3 DAY, '10:00', 'Sem coco no bolo.', 'Cartão', 0.00, 148.00, CURDATE() - INTERVAL 6 DAY);
SET @e = LAST_INSERT_ID();
INSERT INTO item_encomenda (encomenda_id, produto_id, quantidade, preco_momento) VALUES
  (@e, (SELECT id FROM produto WHERE nome = 'Bolo de Cenoura'), 2, 40.00),
  (@e, (SELECT id FROM produto WHERE nome = 'Beijinho'),       30, 2.00);

INSERT INTO encomenda (cliente_id, status, retirada, endereco_entrega, destinatario, frete, data_entrega, hora_entrega, observacao, forma_pagamento, valor_entrada, valor_total, criado_em)
VALUES ((SELECT id FROM cliente WHERE nome = 'Carlos Mendes'), 'preparacao', FALSE, 'Travessa do Sol, 78', 'Carlos Mendes', 12.00,
        CURDATE() + INTERVAL 5 DAY, '12:00', 'Ligar ao chegar.', 'Pix', 60.00, 132.00, CURDATE());
SET @e = LAST_INSERT_ID();
INSERT INTO item_encomenda (encomenda_id, produto_id, quantidade, preco_momento) VALUES
  (@e, (SELECT id FROM produto WHERE nome = 'Pastel de Carne'), 60, 2.00);

INSERT INTO encomenda (cliente_id, status, retirada, endereco_entrega, destinatario, frete, data_entrega, hora_entrega, observacao, forma_pagamento, valor_entrada, valor_total, criado_em)
VALUES ((SELECT id FROM cliente WHERE nome = 'Fernanda Lima'), 'pronto', TRUE, NULL, 'Fernanda Lima', 0.00,
        CURDATE(), '17:00', '', 'Pix', 20.00, 90.00, CURDATE() - INTERVAL 2 DAY);
SET @e = LAST_INSERT_ID();
INSERT INTO item_encomenda (encomenda_id, produto_id, quantidade, preco_momento) VALUES
  (@e, (SELECT id FROM produto WHERE nome = 'Suco Natural 500ml'), 6, 7.50),
  (@e, (SELECT id FROM produto WHERE nome = 'Coxinha de Frango'), 30, 1.50);

INSERT INTO encomenda (cliente_id, status, retirada, endereco_entrega, destinatario, frete, data_entrega, hora_entrega, observacao, forma_pagamento, valor_entrada, valor_total, criado_em)
VALUES ((SELECT id FROM cliente WHERE nome = 'Maria Silva'), 'entregue', FALSE, 'Rua das Flores, 120', 'Maria Silva', 10.00,
        CURDATE() - INTERVAL 10 DAY, '09:00', '', 'Dinheiro', 0.00, 210.00, CURDATE() - INTERVAL 12 DAY);
SET @e = LAST_INSERT_ID();
INSERT INTO item_encomenda (encomenda_id, produto_id, quantidade, preco_momento) VALUES
  (@e, (SELECT id FROM produto WHERE nome = 'Brigadeiro'), 100, 2.00);

-- ---------- Movimentacoes manuais de estoque ----------

INSERT INTO movimentacao (entrada, data_mov, descricao) VALUES (TRUE, CURDATE() - INTERVAL 4 DAY, 'Reposição de salgados');
SET @m = LAST_INSERT_ID();
INSERT INTO item_movimentacao (movimentacao_id, produto_id, quantidade, preco_unit) VALUES
  (@m, (SELECT id FROM produto WHERE nome = 'Coxinha de Frango'), 100, 1.50),
  (@m, (SELECT id FROM produto WHERE nome = 'Pastel de Carne'),    50, 2.00);

INSERT INTO movimentacao (entrada, data_mov, descricao) VALUES (TRUE, CURDATE() - INTERVAL 8 DAY, 'Compra de bebidas');
SET @m = LAST_INSERT_ID();
INSERT INTO item_movimentacao (movimentacao_id, produto_id, quantidade, preco_unit) VALUES
  (@m, (SELECT id FROM produto WHERE nome = 'Refrigerante Lata'), 48, 5.00);

INSERT INTO movimentacao (entrada, data_mov, descricao) VALUES (FALSE, CURDATE() - INTERVAL 1 DAY, 'Perda: produto vencido');
SET @m = LAST_INSERT_ID();
INSERT INTO item_movimentacao (movimentacao_id, produto_id, quantidade, preco_unit) VALUES
  (@m, (SELECT id FROM produto WHERE nome = 'Brigadeiro'), 10, 2.00);

INSERT INTO movimentacao (entrada, data_mov, descricao) VALUES (FALSE, CURDATE(), 'Degustação no balcão');
SET @m = LAST_INSERT_ID();
INSERT INTO item_movimentacao (movimentacao_id, produto_id, quantidade, preco_unit) VALUES
  (@m, (SELECT id FROM produto WHERE nome = 'Beijinho'), 5, 2.00);
