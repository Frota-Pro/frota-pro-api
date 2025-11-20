CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS tb_carga_sync_job (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    empresa_id          UUID NOT NULL,
    data_referencia     DATE NOT NULL,

    status              VARCHAR(20) NOT NULL,

    total_cargas        INTEGER,
    mensagem_erro       VARCHAR(500),

    criado_em           TIMESTAMP WITHOUT TIME ZONE,
    atualizado_em       TIMESTAMP WITHOUT TIME ZONE
);
