ALTER TABLE tb_parada_carga
    ADD COLUMN manutencao_id uuid;

ALTER TABLE tb_parada_carga
    ADD CONSTRAINT uk_parada_manutencao UNIQUE (manutencao_id);

ALTER TABLE tb_parada_carga
    ADD CONSTRAINT fk_parada_manutencao
    FOREIGN KEY (manutencao_id)
    REFERENCES tb_manutencao (id)
    ON UPDATE NO ACTION
    ON DELETE SET NULL;
