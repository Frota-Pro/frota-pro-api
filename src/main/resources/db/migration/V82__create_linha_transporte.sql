-- Linha de transporte do caminhão (ex.: Frios, Linha seca): cadastro livre feito pelo gerente, vinculado
-- ao caminhão, usado pra separar os números no BI. Um caminhão tem no máximo uma linha; sem linha = "Sem linha".
CREATE TABLE IF NOT EXISTS tb_linha_transporte (
    id     uuid         NOT NULL DEFAULT gen_random_uuid(),
    nome   varchar(60)  NOT NULL,
    ativo  boolean      NOT NULL DEFAULT true,

    CONSTRAINT pk_tb_linha_transporte PRIMARY KEY (id)
);

-- nome único sem diferenciar maiúscula ("Frios" e "FRIOS" são a mesma linha)
CREATE UNIQUE INDEX IF NOT EXISTS uk_tb_linha_transporte_nome ON tb_linha_transporte (lower(nome));

ALTER TABLE tb_caminhao
    ADD COLUMN IF NOT EXISTS linha_transporte_id uuid;

ALTER TABLE tb_caminhao
    ADD CONSTRAINT fk_tb_caminhao_tb_linha_transporte
        FOREIGN KEY (linha_transporte_id)
        REFERENCES tb_linha_transporte (id);

CREATE INDEX IF NOT EXISTS idx_tb_caminhao_linha_transporte_id
    ON tb_caminhao (linha_transporte_id);
