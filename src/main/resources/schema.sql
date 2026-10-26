-- SnapSales - script de criacao das tabelas (MySQL)
-- Roda automaticamente ao iniciar a aplicacao. Pode rodar varias vezes (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS categoria (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cliente (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(100) NOT NULL,
    telefone VARCHAR(11)  NOT NULL,
    endereco VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS produto (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome         VARCHAR(100)  NOT NULL,
    quantidade   INT           NOT NULL DEFAULT 0,
    preco        DECIMAL(10,2) NOT NULL DEFAULT 0,
    ativo        BOOLEAN       NOT NULL DEFAULT TRUE,
    categoria_id BIGINT        NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS encomenda (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id       BIGINT        NOT NULL,
    status           VARCHAR(20)   NOT NULL,
    retirada         BOOLEAN       NOT NULL,
    endereco_entrega VARCHAR(200),
    destinatario     VARCHAR(100)  NOT NULL,
    frete            DECIMAL(10,2) NOT NULL DEFAULT 0,
    data_entrega     DATE          NOT NULL,
    hora_entrega     VARCHAR(5)    NOT NULL,
    observacao       VARCHAR(500),
    forma_pagamento  VARCHAR(30)   NOT NULL,
    valor_entrada    DECIMAL(10,2) NOT NULL DEFAULT 0,
    valor_total      DECIMAL(10,2) NOT NULL,
    criado_em        DATE          NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS item_encomenda (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    encomenda_id  BIGINT        NOT NULL,
    produto_id    BIGINT        NOT NULL,
    quantidade    INT           NOT NULL,
    preco_momento DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (encomenda_id) REFERENCES encomenda(id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id)   REFERENCES produto(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS movimentacao (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    entrada   BOOLEAN      NOT NULL,
    data_mov  DATE         NOT NULL,
    descricao VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS item_movimentacao (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    movimentacao_id BIGINT        NOT NULL,
    produto_id      BIGINT        NOT NULL,
    quantidade      INT           NOT NULL,
    preco_unit      DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (movimentacao_id) REFERENCES movimentacao(id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id)      REFERENCES produto(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
