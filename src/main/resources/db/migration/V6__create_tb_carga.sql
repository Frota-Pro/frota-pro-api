CREATE SEQUENCE IF NOT EXISTS seq_carga_numero START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION gerar_numero_carga()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.numero_carga IS NULL OR NEW.numero_carga = '' THEN
        NEW.numero_carga := 'CAR-' || LPAD(nextval('seq_carga_numero')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS tb_carga (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_carga        varchar(50)  NOT NULL,
    numero_carga_externo varchar(50),

    data_faturamento    date,
    data_saida          date,
    data_prevista       date,
    data_chegada        date,

    peso_carga          numeric(15,3),
    km_inicial          integer,
    km_final            integer,

    status_carga        varchar(20)  NOT NULL,

    motorista_id        uuid NOT NULL,
    caminhao_id         uuid NOT NULL,
    rota_id             uuid NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_carga_numero UNIQUE (numero_carga),

    CONSTRAINT fk_carga_motorista
        FOREIGN KEY (motorista_id)
        REFERENCES tb_motorista (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,

    CONSTRAINT fk_carga_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,

    CONSTRAINT fk_carga_rota
        FOREIGN KEY (rota_id)
        REFERENCES tb_rota (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT
);

CREATE TRIGGER tg_carga_numero
BEFORE INSERT ON tb_carga
FOR EACH ROW
EXECUTE FUNCTION gerar_numero_carga();

CREATE TABLE IF NOT EXISTS tb_carga_cliente (
    carga_id uuid       NOT NULL,
    cliente  varchar(150) NOT NULL,

    CONSTRAINT fk_carga_cliente_carga
        FOREIGN KEY (carga_id)
        REFERENCES tb_carga (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT pk_carga_cliente PRIMARY KEY (carga_id, cliente)
);

CREATE TABLE IF NOT EXISTS tb_carga_nota (
    carga_id uuid      NOT NULL,
    nota     varchar(30) NOT NULL,

    CONSTRAINT fk_carga_nota_carga
        FOREIGN KEY (carga_id)
        REFERENCES tb_carga (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT pk_carga_nota PRIMARY KEY (carga_id, nota)
);

CREATE TABLE IF NOT EXISTS tb_carga_ajudante (
    carga_id    uuid NOT NULL,
    ajudante_id uuid NOT NULL,

    CONSTRAINT fk_carga_ajudante_carga
        FOREIGN KEY (carga_id)
        REFERENCES tb_carga (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT fk_carga_ajudante_ajudante
        FOREIGN KEY (ajudante_id)
        REFERENCES tb_ajudante (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,

    CONSTRAINT pk_carga_ajudante PRIMARY KEY (carga_id, ajudante_id)
);
