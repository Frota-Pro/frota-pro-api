ALTER TABLE tb_caminhao
    ADD COLUMN IF NOT EXISTS odometro_ultima_carga integer;

CREATE TABLE IF NOT EXISTS tb_movimentacao_sem_carga (
    id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    caminhao_id             uuid NOT NULL,
    carga_inicio_id         uuid NOT NULL,
    data_movimentacao       date NOT NULL,
    km_origem               integer NOT NULL,
    km_destino              integer NOT NULL,
    km_rodado               integer NOT NULL,
    media_km_litro_usada    numeric(10,3),
    valor_litro_medio       numeric(10,3),
    custo_estimado          numeric(12,2),

    criado_por              varchar(100),
    criado_em               timestamp without time zone,
    atualizado_por          varchar(100),
    atualizado_em           timestamp without time zone,

    CONSTRAINT fk_mov_sem_carga_caminhao
        FOREIGN KEY (caminhao_id)
        REFERENCES tb_caminhao (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,

    CONSTRAINT fk_mov_sem_carga_carga_inicio
        FOREIGN KEY (carga_inicio_id)
        REFERENCES tb_carga (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,

    CONSTRAINT ck_mov_sem_carga_km_rodado
        CHECK (km_rodado > 0),

    CONSTRAINT ck_mov_sem_carga_odometro
        CHECK (km_destino > km_origem)
);

CREATE INDEX IF NOT EXISTS idx_mov_sem_carga_caminhao_data
    ON tb_movimentacao_sem_carga (caminhao_id, data_movimentacao);

CREATE INDEX IF NOT EXISTS idx_mov_sem_carga_data
    ON tb_movimentacao_sem_carga (data_movimentacao);
