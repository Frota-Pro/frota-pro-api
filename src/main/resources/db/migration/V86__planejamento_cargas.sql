-- Planejamento de cargas: a planilha que a logística monta ANTES do faturamento (quais carregamentos do WinThor
-- saem juntos na mesma rota, com qual motorista e caminhão). É um cadastro à parte: a sincronização das cargas, as
-- metas e os relatórios NÃO leem estas tabelas.

CREATE TABLE IF NOT EXISTS tb_planejamento_planilha (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- um dia, uma planilha
    data        DATE         NOT NULL,
    -- ABERTA ou FECHADA (fechada não muda; só reabre no mesmo dia)
    status      VARCHAR(20)  NOT NULL,
    criada_por  VARCHAR(200),
    criada_em   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    fechada_por VARCHAR(200),
    fechada_em  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_planejamento_planilha_data UNIQUE (data)
);

CREATE TABLE IF NOT EXISTS tb_planejamento_item (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    planilha_id         UUID    NOT NULL REFERENCES tb_planejamento_planilha (id) ON DELETE CASCADE,
    -- número do carregamento no WinThor (pccarreg.numcar)
    numero_carregamento INTEGER NOT NULL,
    -- preenchido quando este carregamento é COMPLEMENTO de outro: guarda o número do principal (mesma planilha)
    principal_numero    INTEGER,
    -- retrato do que o WinThor tinha na última atualização (peso em QUILOS)
    destino             VARCHAR(200),
    peso_kg             NUMERIC(15, 3) NOT NULL DEFAULT 0,
    total_pedidos       INTEGER NOT NULL DEFAULT 0,
    -- códigos dos clientes separados por vírgula: a rota conta clientes distintos entre principal e complementos
    clientes_codigos    TEXT,
    faturada            BOOLEAN NOT NULL DEFAULT FALSE,
    data_montagem       DATE,
    -- motorista e caminhão da ROTA: só no carregamento principal
    motorista_id        UUID REFERENCES tb_motorista (id),
    caminhao_id         UUID REFERENCES tb_caminhao (id),
    CONSTRAINT uk_planejamento_item UNIQUE (planilha_id, numero_carregamento)
);

CREATE INDEX IF NOT EXISTS idx_planejamento_item_planilha ON tb_planejamento_item (planilha_id);

-- Carregamentos que o operador mandou "tirar" (ex.: venda balcão). Não voltam a aparecer em nenhuma planilha.
CREATE TABLE IF NOT EXISTS tb_planejamento_removido (
    numero_carregamento INTEGER PRIMARY KEY,
    removido_por        VARCHAR(200),
    removido_em         TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

COMMENT ON TABLE tb_planejamento_planilha IS 'Planilha diária de planejamento de cargas (antes do faturamento). Não influencia a sincronização.';
COMMENT ON TABLE tb_planejamento_item IS 'Carregamentos do WinThor dentro de uma planilha; complemento aponta o principal; motorista/caminhão ficam no principal.';
COMMENT ON TABLE tb_planejamento_removido IS 'Carregamentos tirados do planejamento (não voltam mais).';
