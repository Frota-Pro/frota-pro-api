BEGIN;

ALTER TABLE tb_meta_resultado
    ADD COLUMN IF NOT EXISTS periodo_inicio date,
    ADD COLUMN IF NOT EXISTS periodo_fim date;

UPDATE tb_meta_resultado r
SET periodo_inicio = m.data_inicio,
    periodo_fim = m.data_fim
FROM tb_meta m
WHERE r.meta_id = m.id
  AND (r.periodo_inicio IS NULL OR r.periodo_fim IS NULL);

ALTER TABLE tb_meta_resultado
    ALTER COLUMN periodo_inicio SET NOT NULL,
    ALTER COLUMN periodo_fim SET NOT NULL;

ALTER TABLE tb_meta_resultado
    DROP CONSTRAINT IF EXISTS uk_meta_resultado_meta_caminhao;

CREATE INDEX IF NOT EXISTS idx_meta_resultado_meta_caminhao_periodo
    ON tb_meta_resultado (meta_id, caminhao_id, periodo_inicio, periodo_fim);

COMMIT;
