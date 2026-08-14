ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS senha_temporaria boolean NOT NULL DEFAULT false;
