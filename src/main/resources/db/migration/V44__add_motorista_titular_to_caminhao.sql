ALTER TABLE tb_caminhao
    ADD COLUMN IF NOT EXISTS motorista_titular_id uuid;

ALTER TABLE tb_caminhao
    ADD CONSTRAINT fk_caminhao_motorista_titular
        FOREIGN KEY (motorista_titular_id)
        REFERENCES tb_motorista (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL;

ALTER TABLE tb_caminhao
    ADD CONSTRAINT uk_caminhao_motorista_titular UNIQUE (motorista_titular_id);

CREATE INDEX IF NOT EXISTS idx_caminhao_motorista_titular
    ON tb_caminhao (motorista_titular_id);
