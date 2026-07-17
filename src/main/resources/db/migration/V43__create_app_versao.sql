CREATE TABLE IF NOT EXISTS tb_app_versao (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    versao_nome     varchar(30) NOT NULL,
    notas           text,
    obrigatoria     boolean NOT NULL DEFAULT false,
    ativo           boolean NOT NULL DEFAULT true,
    arquivo_id      uuid NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_app_versao_arquivo
        FOREIGN KEY (arquivo_id)
        REFERENCES tb_arquivo (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_app_versao_ativo
    ON tb_app_versao (ativo)
    WHERE ativo = true;
