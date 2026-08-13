CREATE TABLE tb_posto_abastecimento (
    id UUID PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    cnpj VARCHAR(20),
    cidade VARCHAR(120),
    uf VARCHAR(2),
    endereco VARCHAR(200),
    observacao VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_posto_abastecimento_codigo UNIQUE (codigo)
);

ALTER TABLE tb_abastecimento
    ADD COLUMN IF NOT EXISTS posto_abastecimento_id UUID NULL REFERENCES tb_posto_abastecimento(id);

-- posto (texto livre) deixa de ser a única forma de registrar onde o motorista
-- abasteceu: agora também pode vir vinculado a um posto cadastrado com contrato.
