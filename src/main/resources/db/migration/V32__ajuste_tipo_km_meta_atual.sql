-- V32__ajuste_tipo_km_meta_atual.sql
ALTER TABLE tb_pneu
ALTER COLUMN km_meta_atual TYPE numeric(12,2)
    USING km_meta_atual::numeric;

ALTER TABLE tb_pneu
ALTER COLUMN km_total_acumulado TYPE numeric(12,2)
    USING km_total_acumulado::numeric;
