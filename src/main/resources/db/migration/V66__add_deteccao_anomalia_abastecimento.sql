-- Detecção de anomalia de preço no abastecimento (alerta de possível fraude
-- de cartão combustível): comparado com a média de preço/litro do mesmo
-- posto + tipo de combustível nos últimos 90 dias, calculado uma vez na
-- criação/edição do abastecimento e guardado aqui (mesmo padrão já usado
-- pra media_km_litro nessa tabela).
ALTER TABLE tb_abastecimento
    ADD COLUMN preco_anomalo boolean NOT NULL DEFAULT false,
    ADD COLUMN preco_medio_referencia numeric(10, 3),
    ADD COLUMN preco_anomalo_percentual numeric(6, 1);
