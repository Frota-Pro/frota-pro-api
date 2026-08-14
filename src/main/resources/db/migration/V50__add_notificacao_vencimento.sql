ALTER TABLE tb_motorista
    ADD COLUMN IF NOT EXISTS cnh_notificado_vencimento_em timestamp without time zone;

ALTER TABLE tb_documento_caminhao
    ADD COLUMN IF NOT EXISTS notificado_vencimento_em timestamp without time zone;
