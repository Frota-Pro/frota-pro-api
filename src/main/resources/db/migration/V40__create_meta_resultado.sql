BEGIN;

CREATE TABLE IF NOT EXISTS tb_meta_resultado (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    meta_id         uuid NOT NULL,
    caminhao_id     uuid NOT NULL,
    valor_realizado numeric(15,3),
    periodo_inicio  date NOT NULL,
    periodo_fim     date NOT NULL,
    percentual      numeric(15,2),
    meta_atingida   boolean NOT NULL DEFAULT false,
    calculado_em    timestamp without time zone NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT fk_meta_resultado_meta
        FOREIGN KEY (meta_id)
        REFERENCES tb_meta (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_meta_resultado_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_meta_resultado_meta
    ON tb_meta_resultado (meta_id);

CREATE INDEX IF NOT EXISTS idx_meta_resultado_caminhao
    ON tb_meta_resultado (caminhao_id);

CREATE INDEX IF NOT EXISTS idx_meta_resultado_meta_caminhao_periodo
    ON tb_meta_resultado (meta_id, caminhao_id, periodo_inicio, periodo_fim);

COMMIT;
