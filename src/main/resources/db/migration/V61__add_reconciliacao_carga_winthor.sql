ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS nao_encontrada_no_winthor boolean NOT NULL DEFAULT false;

ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS data_verificacao_winthor timestamp;
