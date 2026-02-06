-- V31__pneus_vida_util.sql
-- Modelo de vida útil por pneu:
-- 1) tb_pneu ganha atributos de cadastro + meta por pneu
-- 2) tb_pneu_instalacao_atual guarda onde o pneu está instalado agora (1 por pneu)
-- 3) tb_pneu_movimentacao registra histórico de eventos do pneu

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- 1) EVOLUI TB_PNEU (já existe desde V10 + V22)
-- =========================================================
ALTER TABLE tb_pneu
    ADD COLUMN IF NOT EXISTS numero_serie        varchar(80),
    ADD COLUMN IF NOT EXISTS marca               varchar(80),
    ADD COLUMN IF NOT EXISTS modelo              varchar(80),
    ADD COLUMN IF NOT EXISTS medida              varchar(40),
    ADD COLUMN IF NOT EXISTS nivel_recapagem     integer,
    ADD COLUMN IF NOT EXISTS status              varchar(20),
    ADD COLUMN IF NOT EXISTS km_meta_atual       integer,
    ADD COLUMN IF NOT EXISTS km_total_acumulado  integer,
    ADD COLUMN IF NOT EXISTS dt_compra           date,
    ADD COLUMN IF NOT EXISTS dt_descarte         date;

-- defaults para registros antigos (se já existirem pneus cadastrados)
UPDATE tb_pneu
SET nivel_recapagem = COALESCE(nivel_recapagem, 0)
WHERE nivel_recapagem IS NULL;

UPDATE tb_pneu
SET km_total_acumulado = COALESCE(km_total_acumulado, 0)
WHERE km_total_acumulado IS NULL;

UPDATE tb_pneu
SET status = COALESCE(status, 'ESTOQUE')
WHERE status IS NULL;

-- se você quiser travar NOT NULL, só faça se tiver certeza que todos os pneus já tem dados
-- ALTER TABLE tb_pneu ALTER COLUMN status SET NOT NULL;
-- ALTER TABLE tb_pneu ALTER COLUMN km_meta_atual SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_pneu_codigo ON tb_pneu (codigo);
CREATE INDEX IF NOT EXISTS idx_pneu_status ON tb_pneu (status);

-- =========================================================
-- 2) INSTALAÇÃO ATUAL (1 linha por pneu)
-- =========================================================
CREATE TABLE IF NOT EXISTS tb_pneu_instalacao_atual (
                                                        id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    pneu_id          uuid NOT NULL UNIQUE,
    caminhao_id      uuid NOT NULL,

    eixo_numero      integer,
    lado             varchar(20),
    posicao          varchar(20),

    km_instalacao    numeric(12,2),
    data_instalacao  timestamp without time zone,

    criado_por       varchar(100),
    criado_em        timestamp without time zone,
    atualizado_por   varchar(100),
    atualizado_em    timestamp without time zone
    );

ALTER TABLE tb_pneu_instalacao_atual
    ADD CONSTRAINT fk_pneu_instalacao_pneu
        FOREIGN KEY (pneu_id)
            REFERENCES tb_pneu (id)
            ON UPDATE NO ACTION
            ON DELETE CASCADE;

ALTER TABLE tb_pneu_instalacao_atual
    ADD CONSTRAINT fk_pneu_instalacao_caminhao
        FOREIGN KEY (caminhao_id)
            REFERENCES tb_caminhao (id)
            ON UPDATE NO ACTION
            ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_pneu_instalacao_caminhao ON tb_pneu_instalacao_atual (caminhao_id);
CREATE INDEX IF NOT EXISTS idx_pneu_instalacao_pneu     ON tb_pneu_instalacao_atual (pneu_id);

-- =========================================================
-- 3) MOVIMENTAÇÕES (histórico)
-- =========================================================
CREATE TABLE IF NOT EXISTS tb_pneu_movimentacao (
                                                    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    pneu_id         uuid NOT NULL,
    tipo            varchar(30) NOT NULL,  -- INSTALACAO | REMOVER | RODIZIO | TROCA_MANUTENCAO | ENVIO_RECAPAGEM | RETORNO_RECAPAGEM | DESCARTE
    data_evento     timestamp without time zone NOT NULL DEFAULT now(),

    km_evento       numeric(12,2),
    observacao      varchar(500),

    caminhao_id     uuid,
    manutencao_id   uuid,
    parada_id       uuid,

    eixo_numero     integer,
    lado            varchar(20),
    posicao         varchar(20),

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone
    );

ALTER TABLE tb_pneu_movimentacao
    ADD CONSTRAINT fk_pneu_mov_pneu
        FOREIGN KEY (pneu_id)
            REFERENCES tb_pneu (id)
            ON UPDATE NO ACTION
            ON DELETE CASCADE;

ALTER TABLE tb_pneu_movimentacao
    ADD CONSTRAINT fk_pneu_mov_caminhao
        FOREIGN KEY (caminhao_id)
            REFERENCES tb_caminhao (id)
            ON UPDATE NO ACTION
            ON DELETE SET NULL;

ALTER TABLE tb_pneu_movimentacao
    ADD CONSTRAINT fk_pneu_mov_manutencao
        FOREIGN KEY (manutencao_id)
            REFERENCES tb_manutencao (id)
            ON UPDATE NO ACTION
            ON DELETE SET NULL;

ALTER TABLE tb_pneu_movimentacao
    ADD CONSTRAINT fk_pneu_mov_parada
        FOREIGN KEY (parada_id)
            REFERENCES tb_parada_carga (id)
            ON UPDATE NO ACTION
            ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_pneu_mov_pneu       ON tb_pneu_movimentacao (pneu_id);
CREATE INDEX IF NOT EXISTS idx_pneu_mov_data       ON tb_pneu_movimentacao (data_evento);
CREATE INDEX IF NOT EXISTS idx_pneu_mov_caminhao   ON tb_pneu_movimentacao (caminhao_id);
CREATE INDEX IF NOT EXISTS idx_pneu_mov_manutencao ON tb_pneu_movimentacao (manutencao_id);
CREATE INDEX IF NOT EXISTS idx_pneu_mov_parada     ON tb_pneu_movimentacao (parada_id);
