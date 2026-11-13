-- Planejamento de cargas: boxes, vários ajudantes por rota e a saída (dia + turno) de cada rota.
-- Tudo continua só planejamento (a sincronização das cargas não lê estas tabelas).

-- Boxes de carregamento (a rota inteira, principal e complementos, usa o mesmo box).
CREATE TABLE IF NOT EXISTS tb_planejamento_box (
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero  INTEGER      NOT NULL,
    nome    VARCHAR(60)  NOT NULL,
    ativo   BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_planejamento_box_numero UNIQUE (numero)
);

INSERT INTO tb_planejamento_box (numero, nome)
SELECT n, 'Box ' || n FROM generate_series(1, 7) AS n
ON CONFLICT (numero) DO NOTHING;

-- Ajudantes da rota (só no carregamento principal): pode ser mais de um.
CREATE TABLE IF NOT EXISTS tb_planejamento_item_ajudante (
    item_id      UUID NOT NULL REFERENCES tb_planejamento_item (id) ON DELETE CASCADE,
    ajudante_id  UUID NOT NULL REFERENCES tb_ajudante (id),
    PRIMARY KEY (item_id, ajudante_id)
);

-- o que já tinha um ajudante só passa para a tabela nova
INSERT INTO tb_planejamento_item_ajudante (item_id, ajudante_id)
SELECT id, ajudante_id FROM tb_planejamento_item WHERE ajudante_id IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE tb_planejamento_item DROP COLUMN IF EXISTS ajudante_id;

-- Box e saída da rota (só no principal).
ALTER TABLE tb_planejamento_item
    ADD COLUMN IF NOT EXISTS box_id UUID REFERENCES tb_planejamento_box (id),
    ADD COLUMN IF NOT EXISTS dia_saida VARCHAR(10),
    ADD COLUMN IF NOT EXISTS turno_saida VARCHAR(12);

COMMENT ON TABLE tb_planejamento_box IS 'Boxes de carregamento usados no planejamento.';
COMMENT ON COLUMN tb_planejamento_item.box_id IS 'Box da ROTA: só no carregamento principal; ocupado até a rota ser faturada.';
COMMENT ON COLUMN tb_planejamento_item.dia_saida IS 'HOJE ou AMANHA (só no principal).';
COMMENT ON COLUMN tb_planejamento_item.turno_saida IS 'MADRUGADA, MANHA, TARDE ou NOITE (só no principal).';
