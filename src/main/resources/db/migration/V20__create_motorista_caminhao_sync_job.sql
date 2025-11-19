CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_motorista_sync_job (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id       uuid        NOT NULL,
    status           varchar(30) NOT NULL,
    mensagem_erro    text,
    total_motoristas integer,
    criado_em        timestamp with time zone NOT NULL,
    atualizado_em    timestamp with time zone NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_motorista_sync_job_empresa
    ON tb_motorista_sync_job (empresa_id);

CREATE INDEX IF NOT EXISTS idx_motorista_sync_job_status
    ON tb_motorista_sync_job (status);

CREATE TABLE IF NOT EXISTS tb_caminhao_sync_job (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id       uuid        NOT NULL,
    status           varchar(30) NOT NULL,
    mensagem_erro    text,
    total_caminhoes  integer,
    criado_em        timestamp with time zone NOT NULL,
    atualizado_em    timestamp with time zone NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_caminhao_sync_job_empresa
    ON tb_caminhao_sync_job (empresa_id);

CREATE INDEX IF NOT EXISTS idx_caminhao_sync_job_status
    ON tb_caminhao_sync_job (status);
