CREATE TABLE tb_cliente (
    id              uuid PRIMARY KEY,
    documento       varchar(20)  NOT NULL,
    nome            varchar(150) NOT NULL,
    logradouro      varchar(200),
    numero          varchar(20),
    complemento     varchar(100),
    bairro          varchar(100),
    cidade          varchar(150),
    uf              varchar(2),
    cep             varchar(9),
    telefone        varchar(20),
    email           varchar(150),
    codigo_externo  varchar(30),

    criado_por      varchar(150),
    criado_em       timestamp,
    atualizado_por  varchar(150),
    atualizado_em   timestamp,

    CONSTRAINT uk_cliente_documento UNIQUE (documento)
);

COMMENT ON TABLE tb_cliente IS
    'Cadastro de cliente de verdade, identificado por CNPJ/CPF - fundacao pra uma futura roteirizacao por endereco. Criado/atualizado a partir do XML da NFe (upload manual ou, pro lado do WinThor, so quando alguem abre o XML de uma nota - nunca durante a sincronizacao em lote).';
COMMENT ON COLUMN tb_cliente.codigo_externo IS
    'codcli do WinThor, quando o cliente foi visto por la - so referencia, nao e chave.';

ALTER TABLE tb_carga_nota
    ADD COLUMN cliente_id uuid REFERENCES tb_cliente (id);

COMMENT ON COLUMN tb_carga_nota.cliente_id IS
    'Vinculo com o cadastro de Cliente, quando ja identificado (por CNPJ/CPF via XML). Nulo ate a nota ser vista/importada com o XML disponivel.';
