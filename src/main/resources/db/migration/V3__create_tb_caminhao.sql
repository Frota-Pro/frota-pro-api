
CREATE SEQUENCE IF NOT EXISTS seq_caminhao_codigo START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION gerar_codigo_caminhao()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'CAM-' || LPAD(nextval('seq_caminhao_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS tb_caminhao (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo              varchar(50)  NOT NULL,
    codigo_externo      varchar(50),
    descricao           varchar(255),
    modelo              varchar(100),
    cor                 varchar(50),
    marca               varchar(100),
    placa               varchar(10),
    antt                varchar(20),
    renavam             varchar(20),
    chassi              varchar(50),

    tara                numeric(10,3),
    max_peso            numeric(10,3),

    data_licenciamento  date,
    seguro              varchar(100),

    status              varchar(20)  NOT NULL,
    ativo               boolean      NOT NULL DEFAULT true,

    criado_por          varchar(100),
    criado_em           timestamp without time zone,
    atualizado_por      varchar(100),
    atualizado_em       timestamp without time zone,

    CONSTRAINT uk_caminhao_codigo  UNIQUE (codigo),
    CONSTRAINT uk_caminhao_placa   UNIQUE (placa),
    CONSTRAINT uk_caminhao_renavam UNIQUE (renavam),
    CONSTRAINT uk_caminhao_chassi  UNIQUE (chassi)
);

CREATE TRIGGER tg_caminhao_codigo
BEFORE INSERT ON tb_caminhao
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_caminhao();
