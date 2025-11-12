CREATE SEQUENCE IF NOT EXISTS seq_ajudante_codigo START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION gerar_codigo_ajudante()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'AJU-' || LPAD(nextval('seq_ajudante_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS tb_ajudante (
    id              uuid PRIMARY KEY,
    codigo          varchar(50)  NOT NULL,
    codigo_externo  varchar(50),
    nome            varchar(150) NOT NULL,

    status          varchar(20)  NOT NULL,
    ativo           boolean      NOT NULL DEFAULT true,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_ajudante_codigo UNIQUE (codigo)
);

CREATE TRIGGER tg_ajudante_codigo
BEFORE INSERT ON tb_ajudante
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_ajudante();