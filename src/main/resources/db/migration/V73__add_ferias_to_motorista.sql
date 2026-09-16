ALTER TABLE tb_motorista
    ADD COLUMN IF NOT EXISTS em_ferias boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS ferias_inicio date,
    ADD COLUMN IF NOT EXISTS ferias_fim_previsto date;
