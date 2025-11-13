CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_despesa_parada (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parada_id       uuid NOT NULL,

    tipo_despesa    varchar(30)  NOT NULL,
    data_hora       timestamp without time zone NOT NULL,
    valor           numeric(12,2) NOT NULL,

    descricao       varchar(255),
    cidade          varchar(150),
    uf              varchar(2),

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_despesa_parada_parada
        FOREIGN KEY (parada_id)
        REFERENCES tb_parada_carga (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);