ALTER TABLE tb_carga_sync_job
    ADD COLUMN origem varchar(30),
    ADD COLUMN solicitado_por varchar(50);

COMMENT ON COLUMN tb_carga_sync_job.origem IS
    'De onde veio o pedido de sincronizacao: API_SCHEDULER (automatico), API_FROTAPRO (manual, tela/endpoint) ou API_RETRY (reprocessamento).';
COMMENT ON COLUMN tb_carga_sync_job.solicitado_por IS
    'Detalha o origem: SCHEDULER (sincronizacao por intervalo), SCHEDULER_REFORCO_MENSAL (reforco diario mes-ate-hoje), sistema (manual) ou USUARIO (retry). Nulo para jobs criados antes desta coluna existir.';
