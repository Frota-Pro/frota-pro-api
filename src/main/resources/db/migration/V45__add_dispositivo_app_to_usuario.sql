ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS dispositivo_app_versao varchar(30);

ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS dispositivo_app_plataforma varchar(20);

ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS dispositivo_app_reportado_em timestamp without time zone;
