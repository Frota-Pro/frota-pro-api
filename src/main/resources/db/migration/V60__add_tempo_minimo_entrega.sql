ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS validar_tempo_minimo_carga boolean NOT NULL DEFAULT false;

ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS tempo_minimo_entrega_padrao_minutos integer NOT NULL DEFAULT 30;

ALTER TABLE tb_roteirizacao_cidade
    ADD COLUMN IF NOT EXISTS tempo_minimo_entrega_minutos integer;

ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS data_hora_saida timestamp;

ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS data_hora_chegada timestamp;
