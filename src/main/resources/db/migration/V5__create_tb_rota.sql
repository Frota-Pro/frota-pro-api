
CREATE SEQUENCE IF NOT EXISTS seq_rota_codigo START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION gerar_codigo_rota()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'ROT-' || LPAD(nextval('seq_rota_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS tb_rota (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo           varchar(50)  NOT NULL,
    cidade_inicio    varchar(150) NOT NULL,
    quantidade_dias  integer,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_rota_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS tb_rota_cidade (
    rota_id uuid       NOT NULL,
    cidade  varchar(150) NOT NULL,

    CONSTRAINT fk_rota_cidade_rota
        FOREIGN KEY (rota_id)
        REFERENCES tb_rota (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT pk_rota_cidade PRIMARY KEY (rota_id, cidade)
);

CREATE TRIGGER tg_rota_codigo
BEFORE INSERT ON tb_rota
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_rota();
