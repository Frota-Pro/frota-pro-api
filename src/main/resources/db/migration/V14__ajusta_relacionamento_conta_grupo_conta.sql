ALTER TABLE tb_grupo_conta
    DROP CONSTRAINT fk_grupo_conta_conta;

ALTER TABLE tb_grupo_conta
    DROP CONSTRAINT uk_grupo_conta_conta;

ALTER TABLE tb_grupo_conta
    DROP COLUMN conta_id;


ALTER TABLE tb_conta
    ADD COLUMN grupo_conta_id uuid;

ALTER TABLE tb_conta
    ADD CONSTRAINT fk_conta_grupo_conta
    FOREIGN KEY (grupo_conta_id)
    REFERENCES tb_grupo_conta (id)
    ON UPDATE NO ACTION
    ON DELETE SET NULL;
