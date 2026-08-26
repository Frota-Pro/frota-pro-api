-- "km_antecedencia_troca_pneu" nunca foi usado pra pneu — é o buffer de
-- antecedência (em km) de TODO plano de manutenção preventiva rastreado por
-- km (troca de óleo, filtro etc.), não só troca de pneu. O nome enganava
-- quem configurava esperando que só afetasse pneu (ver
-- NotificarVencimentosService.estaVencendo). Renomeia pra refletir o uso real.
ALTER TABLE tb_parametro_sistema
    RENAME COLUMN km_antecedencia_troca_pneu TO km_antecedencia_manutencao_preventiva;
