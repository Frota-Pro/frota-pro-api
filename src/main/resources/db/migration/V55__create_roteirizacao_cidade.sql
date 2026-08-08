-- Ordem de entrega parametrizada por cidade: uma lista única de clientes,
-- na ordem em que o motorista deve visitá-los naquela cidade. Aplicada
-- automaticamente nas cargas novas que chegam do WinThor (SincronizarCargaService).
CREATE TABLE IF NOT EXISTS tb_roteirizacao_cidade (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cidade          varchar(150) NOT NULL,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_roteirizacao_cidade_cidade UNIQUE (cidade)
);

CREATE TABLE IF NOT EXISTS tb_roteirizacao_cidade_cliente (
    roteirizacao_cidade_id uuid    NOT NULL,
    ordem                  integer NOT NULL,
    cliente                varchar(200) NOT NULL,

    CONSTRAINT pk_roteirizacao_cidade_cliente PRIMARY KEY (roteirizacao_cidade_id, ordem),
    CONSTRAINT fk_roteirizacao_cidade_cliente_cidade
        FOREIGN KEY (roteirizacao_cidade_id)
        REFERENCES tb_roteirizacao_cidade (id)
        ON DELETE CASCADE
);

-- Clientes da carga que entraram sem posição parametrizada na cidade deles
-- no momento da sincronização — fica registrado nesta carga mesmo que a
-- cidade seja roteirizada depois (só a próxima carga sai correta).
CREATE TABLE IF NOT EXISTS tb_carga_cliente_nao_roteirizado (
    carga_id uuid NOT NULL,
    cliente  varchar(200) NOT NULL,

    CONSTRAINT pk_carga_cliente_nao_roteirizado PRIMARY KEY (carga_id, cliente),
    CONSTRAINT fk_carga_cliente_nao_roteirizado_carga
        FOREIGN KEY (carga_id)
        REFERENCES tb_carga (id)
        ON DELETE CASCADE
);
