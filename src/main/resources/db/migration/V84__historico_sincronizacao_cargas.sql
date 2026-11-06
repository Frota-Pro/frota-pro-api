-- Histórico da sincronização de cargas: o que cada rodada fez e o que mudou em cada carga.
-- Só REGISTRA o que o sync já faz; nenhuma regra de valor/peso depende dessas tabelas.

ALTER TABLE tb_carga_sync_job
    ADD COLUMN IF NOT EXISTS periodo_inicial        DATE,
    ADD COLUMN IF NOT EXISTS periodo_final          DATE,
    ADD COLUMN IF NOT EXISTS qtd_novas              INTEGER,
    ADD COLUMN IF NOT EXISTS qtd_atualizadas        INTEGER,
    ADD COLUMN IF NOT EXISTS qtd_sem_mudanca        INTEGER,
    ADD COLUMN IF NOT EXISTS qtd_descartadas        INTEGER,
    ADD COLUMN IF NOT EXISTS qtd_diminuicao_bloqueada INTEGER;

COMMENT ON COLUMN tb_carga_sync_job.qtd_novas IS 'Cargas criadas por esta rodada.';
COMMENT ON COLUMN tb_carga_sync_job.qtd_atualizadas IS 'Cargas que já existiam e mudaram (valor, peso, status, motorista, caminhão, data ou notas).';
COMMENT ON COLUMN tb_carga_sync_job.qtd_sem_mudanca IS 'Cargas relidas sem nenhuma mudança (não geram item no histórico).';
COMMENT ON COLUMN tb_carga_sync_job.qtd_descartadas IS 'Cargas que não entraram (motorista/caminhão não cadastrado).';
COMMENT ON COLUMN tb_carga_sync_job.qtd_diminuicao_bloqueada IS 'Cargas em que uma diminuição de peso/valor do WinThor foi retida pelo bloqueio.';

CREATE TABLE IF NOT EXISTS tb_carga_sync_item (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id                UUID         NOT NULL,
    numero_carga_externo  VARCHAR(50)  NOT NULL,
    carga_id              UUID,
    -- NOVA, ATUALIZADA, BLOQUEADA (diminuição retida) ou DESCARTADA
    tipo                  VARCHAR(20)  NOT NULL,
    motivo                VARCHAR(500),

    -- peso em TONELADAS (como em tb_carga.peso_carga); valor em R$
    peso_antes            NUMERIC(15,3),
    peso_depois           NUMERIC(15,3),
    valor_antes           NUMERIC(15,3),
    valor_depois          NUMERIC(15,3),
    -- o que o WinThor (minuta) diz nesta rodada; útil quando a diminuição foi retida
    peso_winthor          NUMERIC(15,3),
    valor_winthor         NUMERIC(15,3),

    status_antes          VARCHAR(30),
    status_depois         VARCHAR(30),
    motorista_antes       VARCHAR(200),
    motorista_depois      VARCHAR(200),
    caminhao_antes        VARCHAR(20),
    caminhao_depois       VARCHAR(20),
    dt_faturamento_antes  DATE,
    dt_faturamento_depois DATE,
    notas_antes           INTEGER,
    notas_depois          INTEGER,

    criado_em             TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_carga_sync_item_job ON tb_carga_sync_item (job_id);
CREATE INDEX IF NOT EXISTS idx_carga_sync_item_numero ON tb_carga_sync_item (numero_carga_externo, criado_em DESC);
CREATE INDEX IF NOT EXISTS idx_carga_sync_item_criado ON tb_carga_sync_item (criado_em);
