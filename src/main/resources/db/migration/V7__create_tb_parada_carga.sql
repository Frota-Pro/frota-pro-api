CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_parada_carga (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    carga_id       uuid NOT NULL,

    tipo_parada    varchar(30) NOT NULL,
    dt_inicio      timestamp without time zone NOT NULL,
    dt_fim         timestamp without time zone,

    cidade         varchar(150),
    local          varchar(150),
    km_odometro    integer,
    observacao     varchar(500),

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_parada_carga
        FOREIGN KEY (carga_id)
        REFERENCES tb_carga (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);