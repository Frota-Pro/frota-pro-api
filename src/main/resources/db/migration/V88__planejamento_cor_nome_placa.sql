-- Planejamento de cargas: nome do motorista e placa do veículo que o WinThor gravou no carregamento (para a tela
-- mostrar gente e placa em vez de código) e a cor da rota (índice numa paleta fixa da tela; só rota com complementos).

ALTER TABLE tb_planejamento_item
    ADD COLUMN IF NOT EXISTS nome_motorista_winthor VARCHAR(150),
    ADD COLUMN IF NOT EXISTS placa_veiculo_winthor VARCHAR(20),
    ADD COLUMN IF NOT EXISTS cor INTEGER;

COMMENT ON COLUMN tb_planejamento_item.nome_motorista_winthor IS 'Nome do motorista gravado no carregamento do WinThor (pcempr).';
COMMENT ON COLUMN tb_planejamento_item.placa_veiculo_winthor IS 'Placa do veículo gravado no carregamento do WinThor (pcveicul).';
COMMENT ON COLUMN tb_planejamento_item.cor IS 'Cor da rota (índice da paleta da tela), só no principal que tem complementos.';
