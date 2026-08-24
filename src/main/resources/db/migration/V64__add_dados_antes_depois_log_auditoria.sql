ALTER TABLE tb_log_auditoria
    ADD COLUMN IF NOT EXISTS dados_antes TEXT,
    ADD COLUMN IF NOT EXISTS dados_depois TEXT;

-- Snapshot (JSON) dos campos simples da entidade antes/depois da mudança —
-- populado pra CRIACAO (só dados_depois), ATUALIZACAO (os dois) e EXCLUSAO
-- (só dados_antes). Login/logout continuam sem isso, não se aplica.
