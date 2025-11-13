CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_oficina (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            varchar(150) NOT NULL,
    codigo          varchar(50)  NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_oficina_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS tb_mecanico (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            varchar(150) NOT NULL,
    codigo          varchar(50)  NOT NULL,
    oficina_id      uuid,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_mecanico_codigo UNIQUE (codigo),

    CONSTRAINT fk_mecanico_oficina
        FOREIGN KEY (oficina_id)
        REFERENCES tb_oficina (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS tb_manutencao (
    id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    descricao               varchar(150),
    data_inicio_manutencao  date NOT NULL,
    data_fim_manutencao     date,

    tipo_manutencao         varchar(20) NOT NULL,
    observacoes             varchar(500),
    valor                   numeric(12,2),

    status_manutencao       varchar(20) NOT NULL,

    caminhao_id             uuid NOT NULL,
    oficina_id              uuid,

    criado_por              varchar(100),
    criado_em               timestamp without time zone,
    atualizado_por          varchar(100),
    atualizado_em           timestamp without time zone,

    CONSTRAINT fk_manutencao_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,

    CONSTRAINT fk_manutencao_oficina
        FOREIGN KEY (oficina_id)
        REFERENCES tb_oficina (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS tb_manutencao_item_trocado (
    manutencao_id   uuid NOT NULL,
    item            varchar(150) NOT NULL,

    CONSTRAINT fk_item_trocado_manutencao
        FOREIGN KEY (manutencao_id)
        REFERENCES tb_manutencao (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT pk_manutencao_item_trocado PRIMARY KEY (manutencao_id, item)
);

CREATE TABLE IF NOT EXISTS tb_eixo (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    numero          integer NOT NULL,
    caminhao_id     uuid NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_eixo_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT uk_eixo_caminhao_numero UNIQUE (caminhao_id, numero)
);

CREATE TABLE IF NOT EXISTS tb_pneu (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    posicao             varchar(20),
    data_ultima_troca   date,

    eixo_id             uuid NOT NULL,
    manutencao_id       uuid,

    -- auditoria
    criado_por          varchar(100),
    criado_em           timestamp without time zone,
    atualizado_por      varchar(100),
    atualizado_em       timestamp without time zone,

    CONSTRAINT fk_pneu_eixo
        FOREIGN KEY (eixo_id)
        REFERENCES tb_eixo (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_pneu_manutencao
        FOREIGN KEY (manutencao_id)
        REFERENCES tb_manutencao (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);