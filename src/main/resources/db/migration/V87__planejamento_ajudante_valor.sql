-- Planejamento de cargas: ajudante da rota, valor dos pedidos e o motorista/veículo que o WinThor gravou no
-- carregamento (usado para reconhecer "cliente vem buscar" e filtrar). Continua sendo só planejamento.

ALTER TABLE tb_planejamento_item
    ADD COLUMN IF NOT EXISTS ajudante_id UUID REFERENCES tb_ajudante (id),
    ADD COLUMN IF NOT EXISTS valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS cod_motorista_winthor INTEGER,
    ADD COLUMN IF NOT EXISTS cod_veiculo_winthor INTEGER;

COMMENT ON COLUMN tb_planejamento_item.ajudante_id IS 'Ajudante da ROTA: só no carregamento principal.';
COMMENT ON COLUMN tb_planejamento_item.valor_total IS 'Soma do valor dos pedidos do carregamento (WinThor), em reais.';
COMMENT ON COLUMN tb_planejamento_item.cod_motorista_winthor IS 'Motorista gravado no carregamento do WinThor (ex.: 10 = cliente vem buscar).';
COMMENT ON COLUMN tb_planejamento_item.cod_veiculo_winthor IS 'Veículo gravado no carregamento do WinThor (ex.: 2 = cliente vem buscar).';
