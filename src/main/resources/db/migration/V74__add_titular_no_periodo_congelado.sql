-- "Foto" do motorista titular do caminhão no momento em que a carga/
-- abastecimento foi criado — usado pra calcular km rodado/km-por-litro nos
-- relatorios e metas, pra que uma troca de titular HOJE nao mude os numeros
-- de meses ja fechados (ver TransferirTitularCaminhaoService/
-- SincronizarCargaService/CriarAbastecimentoService).

ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS titular_no_periodo_id uuid;

ALTER TABLE tb_carga
    ADD CONSTRAINT fk_carga_titular_no_periodo
        FOREIGN KEY (titular_no_periodo_id)
        REFERENCES tb_motorista (id)
        ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_carga_titular_no_periodo
    ON tb_carga (titular_no_periodo_id);

ALTER TABLE tb_abastecimento
    ADD COLUMN IF NOT EXISTS titular_no_periodo_id uuid;

ALTER TABLE tb_abastecimento
    ADD CONSTRAINT fk_abastecimento_titular_no_periodo
        FOREIGN KEY (titular_no_periodo_id)
        REFERENCES tb_motorista (id)
        ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_abastecimento_titular_no_periodo
    ON tb_abastecimento (titular_no_periodo_id);

-- Backfill: nao ha como saber quem era titular na epoca de cargas/
-- abastecimentos ja existentes, entao assumimos o titular ATUAL do
-- caminhao pra eles (mesma leitura que o sistema ja fazia antes desta
-- mudanca). So passa a "congelar" de fato a partir de agora.
UPDATE tb_carga c
SET titular_no_periodo_id = cam.motorista_titular_id
FROM tb_caminhao cam
WHERE c.caminhao_id = cam.id
  AND cam.motorista_titular_id IS NOT NULL
  AND c.titular_no_periodo_id IS NULL;

UPDATE tb_abastecimento a
SET titular_no_periodo_id = cam.motorista_titular_id
FROM tb_caminhao cam
WHERE a.caminhao_id = cam.id
  AND cam.motorista_titular_id IS NOT NULL
  AND a.titular_no_periodo_id IS NULL;
