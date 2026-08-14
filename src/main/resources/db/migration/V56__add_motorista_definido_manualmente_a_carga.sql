-- Quando o motorista de uma carga é trocado manualmente (a carga foi
-- faturada pra um motorista no WinThor, mas outro foi quem realmente saiu
-- com ela, e o MDF-e/minuta não muda pra refletir isso), esse flag impede
-- que a próxima sincronização do WinThor sobrescreva a troca.
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS motorista_definido_manualmente boolean NOT NULL DEFAULT false;
