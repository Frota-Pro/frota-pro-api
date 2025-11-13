CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SEQUENCE IF NOT EXISTS seq_abastecimento_codigo START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION gerar_codigo_abastecimento()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'ABS-' || LPAD(nextval('seq_abastecimento_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TABLE IF NOT EXISTS tb_abastecimento (
    id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo                  varchar(50) NOT NULL,

    parada_id               uuid,
    caminhao_id             uuid NOT NULL,
    motorista_id            uuid,

    dt_abastecimento        timestamp without time zone NOT NULL,
    km_odometro             integer,

    qt_litros               numeric(10,3),
    valor_litro             numeric(10,3),
    valor_total             numeric(12,2),

    tipo_combustivel        varchar(20) NOT NULL,
    forma_pagamento         varchar(20) NOT NULL,

    posto                   varchar(150),
    cidade                  varchar(150),
    uf                      varchar(2),

    numero_nota_cupom       varchar(30),

    media_km_litro          numeric(10,3),

    criado_por              varchar(100),
    criado_em               timestamp without time zone,
    atualizado_por          varchar(100),
    atualizado_em           timestamp without time zone,

    CONSTRAINT uk_abastecimento_codigo UNIQUE (codigo),

    CONSTRAINT fk_abastecimento_parada FOREIGN KEY (parada_id)
        REFERENCES tb_parada_carga (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL,

    CONSTRAINT fk_abastecimento_caminhao FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,

    CONSTRAINT fk_abastecimento_motorista FOREIGN KEY (motorista_id)
        REFERENCES tb_motorista (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);

CREATE TRIGGER tg_abastecimento_codigo
BEFORE INSERT ON tb_abastecimento
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_abastecimento();
