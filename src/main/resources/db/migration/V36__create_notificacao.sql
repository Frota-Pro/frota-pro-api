CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_notificacao (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    evento            varchar(40) NOT NULL,
    tipo              varchar(20) NOT NULL,
    titulo            varchar(180) NOT NULL,
    mensagem          varchar(2000) NOT NULL,
    referencia_tipo   varchar(40),
    referencia_id     uuid,
    referencia_codigo varchar(100),

    criado_por        varchar(100),
    criado_em         timestamp without time zone,
    atualizado_por    varchar(100),
    atualizado_em     timestamp without time zone
);

CREATE INDEX IF NOT EXISTS ix_notificacao_criado_em
    ON tb_notificacao (criado_em DESC);

CREATE TABLE IF NOT EXISTS tb_notificacao_usuario (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacao_id  uuid NOT NULL,
    usuario_id      uuid NOT NULL,
    lida_em         timestamp without time zone,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_notificacao_usuario_notificacao
        FOREIGN KEY (notificacao_id)
        REFERENCES tb_notificacao (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_notificacao_usuario_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES tb_usuario (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT uk_notificacao_usuario UNIQUE (notificacao_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS ix_notificacao_usuario_usuario
    ON tb_notificacao_usuario (usuario_id);

CREATE INDEX IF NOT EXISTS ix_notificacao_usuario_lida_em
    ON tb_notificacao_usuario (lida_em);
