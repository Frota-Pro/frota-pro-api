-- Quando o caminhão de uma carga é corrigido manualmente (o MDF-e foi emitido no
-- WinThor com o veículo errado e não dá pra reemitir), esse flag impede que a próxima
-- sincronização do WinThor desfaça a correção.
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS caminhao_definido_manualmente boolean NOT NULL DEFAULT false;
