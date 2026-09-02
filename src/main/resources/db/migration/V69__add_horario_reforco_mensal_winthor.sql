ALTER TABLE tb_integracao_winthor_config
    ADD COLUMN horario_reforco_mensal TIME NOT NULL DEFAULT '20:00:00';

COMMENT ON COLUMN tb_integracao_winthor_config.horario_reforco_mensal IS
    'Horario diario (fuso America/Recife) em que, alem da sincronizacao automatica normal (so o dia de hoje), roda um reforco cobrindo do dia 1 do mes ate hoje - pega devolucoes/transferencias do WinThor lancadas depois da carga ja ter sido sincronizada em dias anteriores do mes.';
