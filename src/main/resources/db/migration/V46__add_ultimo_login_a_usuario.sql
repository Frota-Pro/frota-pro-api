ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS ultimo_login_em timestamp without time zone;

ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS total_logins bigint NOT NULL DEFAULT 0;
