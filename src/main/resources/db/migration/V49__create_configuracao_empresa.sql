CREATE TABLE tb_configuracao_empresa (
    id uuid NOT NULL PRIMARY KEY,
    empresa_id uuid NOT NULL,
    nome_empresa varchar(150),
    logo_id uuid REFERENCES tb_arquivo (id),
    email_remetente varchar(150),
    email_assunto varchar(200),
    email_corpo_html text,
    criado_por varchar(255),
    criado_em timestamp without time zone,
    atualizado_por varchar(255),
    atualizado_em timestamp without time zone,
    CONSTRAINT uk_configuracao_empresa_empresa UNIQUE (empresa_id)
);
