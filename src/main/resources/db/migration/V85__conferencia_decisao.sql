-- Decisões da tela de Conferência: o que o usuário confirmou ou mandou ignorar em cada tipo de conferência.
-- Só REGISTRA a decisão (e some com o item da lista); a sincronização das cargas não lê esta tabela.

CREATE TABLE IF NOT EXISTS tb_conferencia_decisao (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- tipo da conferência (ex.: REENTREGA_MOTORISTA_CAMINHAO)
    tipo          VARCHAR(60)  NOT NULL,
    -- o que foi conferido (ex.: número do carregamento no WinThor)
    chave         VARCHAR(60)  NOT NULL,
    -- CONFIRMADA ou IGNORADA
    decisao       VARCHAR(20)  NOT NULL,
    -- resumo do que o sistema propunha; um "ignorar" só vale para a mesma proposta
    assinatura    VARCHAR(200),
    -- o que foi feito (antes -> depois), para consulta
    detalhe       VARCHAR(1000),
    decidido_por  VARCHAR(200),
    decidido_em   TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_conferencia_decisao_tipo_chave ON tb_conferencia_decisao (tipo, chave, decidido_em DESC);

COMMENT ON TABLE tb_conferencia_decisao IS 'Decisões do usuário na tela de Conferência (confirmou/ignorou). Não influencia a sincronização.';
