CREATE TABLE tb_multa (
    id uuid NOT NULL PRIMARY KEY,
    caminhao_id uuid NOT NULL REFERENCES tb_caminhao (id),
    motorista_id uuid REFERENCES tb_motorista (id),
    data_infracao date NOT NULL,
    orgao_autuador varchar(100),
    numero_ait varchar(50),
    descricao_infracao varchar(255),
    gravidade varchar(20),
    pontos integer,
    valor numeric(12, 2) NOT NULL,
    data_vencimento_pagamento date,
    data_limite_recurso date,
    status_pagamento varchar(30) NOT NULL,
    responsavel_pagamento varchar(20) NOT NULL,
    observacao varchar(500),
    notificado_prazo_em timestamp without time zone,
    criado_por varchar(255),
    criado_em timestamp without time zone,
    atualizado_por varchar(255),
    atualizado_em timestamp without time zone
);

CREATE INDEX idx_multa_caminhao ON tb_multa (caminhao_id);
CREATE INDEX idx_multa_motorista ON tb_multa (motorista_id);
CREATE INDEX idx_multa_status_pagamento ON tb_multa (status_pagamento);

CREATE TABLE tb_multa_anexo (
    id uuid NOT NULL PRIMARY KEY,
    multa_id uuid NOT NULL REFERENCES tb_multa (id),
    arquivo_id uuid NOT NULL REFERENCES tb_arquivo (id),
    tipo_anexo varchar(30) NOT NULL,
    criado_por varchar(255),
    criado_em timestamp without time zone,
    atualizado_por varchar(255),
    atualizado_em timestamp without time zone
);

CREATE INDEX idx_multa_anexo_multa ON tb_multa_anexo (multa_id);
