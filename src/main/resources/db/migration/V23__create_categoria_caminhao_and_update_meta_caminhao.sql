BEGIN;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";


CREATE TABLE IF NOT EXISTS tb_categoria_caminhao (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    codigo      VARCHAR(20)     NOT NULL,
    descricao   VARCHAR(100)    NOT NULL,
    observacao  VARCHAR(255),
    ativo       BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_tb_categoria_caminhao PRIMARY KEY (id),
    CONSTRAINT uk_tb_categoria_caminhao_codigo UNIQUE (codigo)
);

ALTER TABLE tb_caminhao
    ADD COLUMN IF NOT EXISTS categoria_caminhao_id UUID;

ALTER TABLE tb_caminhao
    ADD CONSTRAINT fk_tb_caminhao_tb_categoria_caminhao
        FOREIGN KEY (categoria_caminhao_id)
        REFERENCES tb_categoria_caminhao (id);

CREATE INDEX IF NOT EXISTS idx_tb_caminhao_categoria_caminhao_id
    ON tb_caminhao (categoria_caminhao_id);


ALTER TABLE tb_meta
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(20);

ALTER TABLE tb_meta
    ADD CONSTRAINT uk_tb_meta_codigo UNIQUE (codigo);

ALTER TABLE tb_meta
    ADD COLUMN IF NOT EXISTS categoria_caminhao_id UUID;

ALTER TABLE tb_meta
    ADD CONSTRAINT fk_tb_meta_tb_categoria_caminhao
        FOREIGN KEY (categoria_caminhao_id)
        REFERENCES tb_categoria_caminhao (id);

CREATE INDEX IF NOT EXISTS idx_tb_meta_categoria_caminhao_id
    ON tb_meta (categoria_caminhao_id);


CREATE SEQUENCE IF NOT EXISTS seq_meta_codigo
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE OR REPLACE FUNCTION gerar_codigo_meta()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'META-' || LPAD(nextval('seq_meta_codigo')::text, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_meta_gerar_codigo ON tb_meta;

CREATE TRIGGER trg_meta_gerar_codigo
    BEFORE INSERT ON tb_meta
    FOR EACH ROW
    EXECUTE FUNCTION gerar_codigo_meta();

COMMIT;
