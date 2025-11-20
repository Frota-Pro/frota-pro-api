
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SEQUENCE IF NOT EXISTS seq_motorista_codigo START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION gerar_codigo_motorista()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'MOT-' || LPAD(nextval('seq_motorista_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS tb_motorista (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo           varchar(50)  NOT NULL,
    codigo_externo   varchar(50),
    nome             varchar(150) NOT NULL,
    email            varchar(150),
    data_nascimento  date,
    cnh              varchar(11),
    validade_cnh     date,
    status           varchar(20)  NOT NULL,
    ativo            boolean      NOT NULL DEFAULT true,

    usuario_id       uuid NULL,

    criado_por       varchar(100),
    criado_em        timestamp without time zone,
    atualizado_por   varchar(100),
    atualizado_em    timestamp without time zone,

    CONSTRAINT uk_motorista_codigo   UNIQUE (codigo),
    CONSTRAINT uk_motorista_cnh      UNIQUE (cnh),
    CONSTRAINT uk_motorista_email    UNIQUE (email),
    CONSTRAINT uk_motorista_usuario  UNIQUE (usuario_id),

    CONSTRAINT fk_motorista_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES tb_usuario (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);

CREATE TRIGGER tg_motorista_codigo
BEFORE INSERT ON tb_motorista
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_motorista();
