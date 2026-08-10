ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS codigos_devolucao_encontrados varchar(500);

ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS teve_transferencia boolean NOT NULL DEFAULT false;

ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS diminuicao_peso_valor_bloqueada boolean NOT NULL DEFAULT false;
