CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_acesso (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nome         varchar(50)  NOT NULL,
    descrisao    varchar(150) NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_acesso_nome UNIQUE (nome)
);

COMMENT ON TABLE tb_acesso IS 'Tabela de perfis/roles de acesso';
COMMENT ON COLUMN tb_acesso.nome IS 'Nome único do acesso/perfil';


CREATE TABLE IF NOT EXISTS tb_usuario (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    login        varchar(50)  NOT NULL,
    senha        varchar(150) NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_usuario_login UNIQUE (login)
);

COMMENT ON TABLE tb_usuario IS 'Tabela de usuários do sistema';
COMMENT ON COLUMN tb_usuario.login IS 'Login único do usuário';


CREATE TABLE IF NOT EXISTS tb_usuario_acesso (
    usuario_id uuid NOT NULL,
    acesso_id  uuid NOT NULL,

    CONSTRAINT fk_usuario_acesso_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES tb_usuario (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_acesso_acesso
        FOREIGN KEY (acesso_id)
        REFERENCES tb_acesso (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,

    CONSTRAINT pk_usuario_acesso PRIMARY KEY (usuario_id, acesso_id)
);