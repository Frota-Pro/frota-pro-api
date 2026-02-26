BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_meta_categoria_caminhao_vinculo (
    id                           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    meta_id                      uuid NOT NULL,
    caminhao_id                  uuid NOT NULL,
    caminhao_codigo_snapshot     varchar(50) NOT NULL,
    caminhao_descricao_snapshot  varchar(255),

    criado_por                   varchar(100),
    criado_em                    timestamp without time zone,
    atualizado_por               varchar(100),
    atualizado_em                timestamp without time zone,

    CONSTRAINT fk_meta_cat_cam_vinculo_meta
        FOREIGN KEY (meta_id)
        REFERENCES tb_meta (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_meta_cat_cam_vinculo_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT uk_meta_cat_cam_vinculo_meta_caminhao UNIQUE (meta_id, caminhao_id)
);

CREATE INDEX IF NOT EXISTS idx_meta_cat_cam_vinculo_meta
    ON tb_meta_categoria_caminhao_vinculo (meta_id);

CREATE INDEX IF NOT EXISTS idx_meta_cat_cam_vinculo_caminhao
    ON tb_meta_categoria_caminhao_vinculo (caminhao_id);

-- Backfill para metas de categoria já existentes:
-- vincula os caminhões ativos da categoria atual no momento da migração.
INSERT INTO tb_meta_categoria_caminhao_vinculo (meta_id, caminhao_id, caminhao_codigo_snapshot, caminhao_descricao_snapshot)
SELECT
    m.id,
    c.id,
    c.codigo,
    c.descricao
FROM tb_meta m
JOIN tb_caminhao c
  ON c.categoria_caminhao_id = m.categoria_caminhao_id
 AND c.ativo = TRUE
WHERE m.categoria_caminhao_id IS NOT NULL
ON CONFLICT (meta_id, caminhao_id) DO NOTHING;

COMMIT;
