CREATE SEQUENCE IF NOT EXISTS seq_motorista_codigo START 1;

CREATE OR REPLACE FUNCTION gerar_codigo_motorista()
RETURNS TRIGGER AS $$
BEGIN
    NEW.codigo := 'MTR-' || LPAD(nextval('seq_motorista_codigo')::TEXT, 6, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TABLE IF NOT EXISTS tb_motorista(
    id UUID PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    codigo_externo VARCHAR(50),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    data_nascimento DATE NOT NULL,
    cnh VARCHAR(11) NOT NULL,
    validade_cnh DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    -- usuario_id UUID NULL,

    criado_por VARCHAR(100),
    criado_em TIMESTAMP WITHOUT TIME ZONE,
    atualizado_por VARCHAR(100),
    atualizado_em TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT uk_motorista_codigo UNIQUE (codigo),
    CONSTRAINT uk_motorista_cnh    UNIQUE (cnh),
    CONSTRAINT uk_motorista_email  UNIQUE (email)
    -- CONSTRAINT uk_motorista_usuario UNIQUE (usuario_id)

  -- CONSTRAINT fk_motorista_usuario
        --FOREIGN KEY (usuario_id)
        --REFERENCES tb_usuario (id)
        --ON UPDATE NO ACTION
        --ON DELETE SET NULL
);

CREATE TRIGGER tg_motorista_codigo
BEFORE INSERT ON tb_motorista
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_motorista();