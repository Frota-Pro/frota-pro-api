CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Itens detalhados da manutenção (peças/serviços)
CREATE TABLE IF NOT EXISTS tb_manutencao_item (
                                                  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    manutencao_id    uuid NOT NULL,

    tipo            varchar(20) NOT NULL,
    descricao       varchar(200) NOT NULL,
    quantidade      numeric(12,4) NOT NULL,
    valor_unitario  numeric(12,2) NOT NULL,
    valor_total     numeric(12,2) NOT NULL,

    -- auditoria
    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_manutencao_item_manutencao
    FOREIGN KEY (manutencao_id)
    REFERENCES tb_manutencao (id)
                              ON UPDATE NO ACTION
                              ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS ix_manutencao_item_manutencao_id
    ON tb_manutencao_item (manutencao_id);

-- Documentos/anexos da manutenção
CREATE TABLE IF NOT EXISTS tb_documento_manutencao (
                                                       id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    manutencao_id   uuid NOT NULL,
    arquivo_id      uuid NOT NULL,
    tipo_documento  varchar(30) NOT NULL,
    observacao      varchar(255),

    -- auditoria
    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_doc_manutencao_manutencao
    FOREIGN KEY (manutencao_id)
    REFERENCES tb_manutencao (id)
                              ON UPDATE NO ACTION
                              ON DELETE CASCADE,

    CONSTRAINT fk_doc_manutencao_arquivo
    FOREIGN KEY (arquivo_id)
    REFERENCES tb_arquivo (id)
                              ON UPDATE NO ACTION
                              ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS ix_doc_manutencao_manutencao_id
    ON tb_documento_manutencao (manutencao_id);
