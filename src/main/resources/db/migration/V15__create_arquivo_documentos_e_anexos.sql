CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_arquivo (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_original   varchar(255) NOT NULL,
    caminho         varchar(500) NOT NULL,
    content_type    varchar(100),
    tamanho_bytes   bigint,
    hash            varchar(100),

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone
);

CREATE TABLE IF NOT EXISTS tb_documento_caminhao (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    caminhao_id     uuid NOT NULL,
    arquivo_id      uuid NOT NULL,
    tipo_documento  varchar(30) NOT NULL,
    data_validade   date,
    observacao      varchar(255),

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_doc_caminhao_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_doc_caminhao_arquivo
        FOREIGN KEY (arquivo_id)
        REFERENCES tb_arquivo (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tb_documento_motorista (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    motorista_id    uuid NOT NULL,
    arquivo_id      uuid NOT NULL,
    tipo_documento  varchar(30) NOT NULL,
    data_validade   date,
    observacao      varchar(255),

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_doc_motorista_motorista
        FOREIGN KEY (motorista_id)
        REFERENCES tb_motorista (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_doc_motorista_arquivo
        FOREIGN KEY (arquivo_id)
        REFERENCES tb_arquivo (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tb_anexo_parada (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parada_id       uuid NOT NULL,
    arquivo_id      uuid NOT NULL,
    tipo_anexo      varchar(40) NOT NULL,
    observacao      varchar(255),

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_anexo_parada_parada
        FOREIGN KEY (parada_id)
        REFERENCES tb_parada_carga (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_anexo_parada_arquivo
        FOREIGN KEY (arquivo_id)
        REFERENCES tb_arquivo (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);
