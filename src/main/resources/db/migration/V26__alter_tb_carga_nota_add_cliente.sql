ALTER TABLE tb_carga_nota
    ADD COLUMN cliente VARCHAR(150);

UPDATE tb_carga_nota
SET cliente = 'MIGRACAO'
WHERE cliente IS NULL;

ALTER TABLE tb_carga_nota
    ALTER COLUMN cliente SET NOT NULL;

ALTER TABLE tb_carga_nota
    DROP CONSTRAINT IF EXISTS pk_carga_nota;

ALTER TABLE tb_carga_nota
    DROP CONSTRAINT IF EXISTS tb_carga_nota_pkey;

ALTER TABLE tb_carga_nota
    ADD CONSTRAINT pk_carga_nota
        PRIMARY KEY (carga_id, cliente, nota);
