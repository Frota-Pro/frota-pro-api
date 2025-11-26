CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE tb_pneu
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(20);

ALTER TABLE tb_pneu
    ADD COLUMN IF NOT EXISTS km_ultima_troca INTEGER,
    ADD COLUMN IF NOT EXISTS lado_atual      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS posicao_atual   VARCHAR(20);

ALTER TABLE tb_pneu
    DROP CONSTRAINT IF EXISTS fk_pneu_manutencao;

ALTER TABLE tb_pneu
    DROP COLUMN IF EXISTS manutencao_id;


CREATE SEQUENCE IF NOT EXISTS seq_pneu_codigo START 1;

CREATE OR REPLACE FUNCTION fn_gerar_codigo_pneu()
RETURNS trigger AS
$$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'PNEU-' || LPAD(nextval('seq_pneu_codigo')::text, 6, '0');
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_gerar_codigo_pneu ON tb_pneu;

CREATE TRIGGER trg_gerar_codigo_pneu
BEFORE INSERT ON tb_pneu
FOR EACH ROW
EXECUTE FUNCTION fn_gerar_codigo_pneu();

UPDATE tb_pneu
SET codigo = 'PNEU-' || LPAD(nextval('seq_pneu_codigo')::text, 6, '0')
WHERE codigo IS NULL;

ALTER TABLE tb_pneu
    ALTER COLUMN codigo SET NOT NULL;

ALTER TABLE tb_pneu
    ADD CONSTRAINT uk_pneu_codigo UNIQUE (codigo);


CREATE TABLE IF NOT EXISTS tb_troca_pneu_manutencao (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    manutencao_id   uuid NOT NULL,
    pneu_id         uuid NOT NULL,
    eixo_id         uuid NOT NULL,

    lado            varchar(20) NOT NULL,
    posicao         varchar(20) NOT NULL,
    km_odometro     integer     NOT NULL,
    tipo_troca      varchar(20) NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone
);

ALTER TABLE tb_troca_pneu_manutencao
    ADD CONSTRAINT fk_trocapneu_manutencao
        FOREIGN KEY (manutencao_id)
        REFERENCES tb_manutencao (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT;

ALTER TABLE tb_troca_pneu_manutencao
    ADD CONSTRAINT fk_trocapneu_pneu
        FOREIGN KEY (pneu_id)
        REFERENCES tb_pneu (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

ALTER TABLE tb_troca_pneu_manutencao
    ADD CONSTRAINT fk_trocapneu_eixo
        FOREIGN KEY (eixo_id)
        REFERENCES tb_eixo (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_trocapneu_manutencao
    ON tb_troca_pneu_manutencao (manutencao_id);

CREATE INDEX IF NOT EXISTS idx_trocapneu_pneu
    ON tb_troca_pneu_manutencao (pneu_id);

CREATE INDEX IF NOT EXISTS idx_trocapneu_eixo
    ON tb_troca_pneu_manutencao (eixo_id);

CREATE INDEX IF NOT EXISTS idx_trocapneu_km
    ON tb_troca_pneu_manutencao (km_odometro);
