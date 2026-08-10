ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS validar_motivo_alteracao_peso_valor_carga boolean NOT NULL DEFAULT false;

ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS codigos_devolucao_permitidos varchar(500);

ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS permitir_atualizacao_por_transferencia boolean NOT NULL DEFAULT true;
