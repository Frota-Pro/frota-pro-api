-- Cargas que o sync com o WinThor descartou (motorista ou caminhão do MDF-e
-- ainda não cadastrado no FrotaPRO). Uma linha por carga (numero_carga_externo):
-- reincidência só atualiza a linha, e ela some assim que a carga sincroniza de
-- verdade. Existe pra o gestor enxergar o que ficou de fora, em vez de depender
-- de uma linha de log.
CREATE TABLE IF NOT EXISTS tb_carga_sync_descartada (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_carga_externo     varchar(50)  NOT NULL,
    num_mdfe                 bigint,
    situacao_mdfe            varchar(10),
    motivo                   varchar(40)  NOT NULL,

    codigo_motorista_externo varchar(50),
    nome_motorista_externo   varchar(200),
    codigo_veiculo_externo   varchar(50),
    placa_veiculo_externo    varchar(20),

    dt_saida                 date,
    peso_carga               numeric(14, 3),
    valor_total              numeric(14, 2),
    destino                  varchar(120),
    total_clientes           integer,

    tentativas               integer      NOT NULL DEFAULT 1,
    primeira_ocorrencia_em   timestamp without time zone NOT NULL,
    ultima_ocorrencia_em     timestamp without time zone NOT NULL,

    CONSTRAINT uk_carga_sync_descartada_numcar UNIQUE (numero_carga_externo)
);

CREATE INDEX IF NOT EXISTS idx_carga_sync_descartada_ultima
    ON tb_carga_sync_descartada (ultima_ocorrencia_em DESC);
