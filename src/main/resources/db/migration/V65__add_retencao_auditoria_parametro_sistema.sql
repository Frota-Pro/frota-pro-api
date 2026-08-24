ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS dias_retencao_auditoria INTEGER NOT NULL DEFAULT 180;

-- Quantos dias um registro fica em tb_log_auditoria antes de ser apagado
-- automaticamente (job diário) — sem isso a tabela cresce sem limite, já
-- que toda ação de escrita do sistema gera uma linha.
