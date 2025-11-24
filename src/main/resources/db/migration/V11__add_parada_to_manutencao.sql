ALTER TABLE tb_manutencao
    ADD COLUMN parada_id uuid;

ALTER TABLE tb_manutencao
    ADD CONSTRAINT fk_manutencao_parada
        FOREIGN KEY (parada_id)
        REFERENCES tb_parada_carga (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL;
