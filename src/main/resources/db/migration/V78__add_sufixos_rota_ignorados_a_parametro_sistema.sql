-- O destino da carga vem do WinThor como texto livre ("SOUSA", "SOUSA FRIOS"). Palavras
-- listadas aqui, no fim do destino, são ignoradas ao achar a rota: "SOUSA FRIOS" cai na
-- rota "SOUSA". Separadas por vírgula; vazio = nenhuma (cada grafia vira uma rota).
ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS sufixos_rota_ignorados varchar(200) DEFAULT 'FRIOS';
