CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_conta (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo          varchar(50)  NOT NULL,
    codigo_externo  varchar(50),
    nome            varchar(150) NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_conta_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS tb_grupo_conta (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo          varchar(50)  NOT NULL,
    codigo_externo  varchar(50),
    nome            varchar(150) NOT NULL,

    conta_id        uuid,
    caminhao_id     uuid,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_grupo_conta_codigo UNIQUE (codigo),

    CONSTRAINT fk_grupo_conta_conta
        FOREIGN KEY (conta_id)
        REFERENCES tb_conta (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL,

    CONSTRAINT fk_grupo_conta_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL,

    CONSTRAINT uk_grupo_conta_conta UNIQUE (conta_id),
    CONSTRAINT uk_grupo_conta_caminhao UNIQUE (caminhao_id)
);

CREATE TABLE IF NOT EXISTS tb_meta (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    data_inicio     date,
    data_fim        date,

    tipo_meta       varchar(30) NOT NULL,
    valor_meta      numeric(15,3),
    valor_realizado numeric(15,3),

    unidade         varchar(50),
    status_meta     varchar(20) NOT NULL,
    descricao       varchar(150),

    caminhao_id     uuid,
    motorista_id    uuid,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_meta_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL,

    CONSTRAINT fk_meta_motorista
        FOREIGN KEY (motorista_id)
        REFERENCES tb_motorista (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);
