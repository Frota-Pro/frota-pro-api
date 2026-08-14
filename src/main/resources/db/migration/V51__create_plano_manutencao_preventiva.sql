CREATE TABLE tb_plano_manutencao_preventiva (
    id uuid NOT NULL PRIMARY KEY,
    caminhao_id uuid NOT NULL REFERENCES tb_caminhao (id),
    descricao varchar(150) NOT NULL,
    intervalo_km integer,
    intervalo_dias integer,
    ativo boolean NOT NULL DEFAULT true,
    ultimo_km_executado integer,
    ultima_data_executada date,
    notificado_vencimento_em timestamp without time zone,
    criado_por varchar(255),
    criado_em timestamp without time zone,
    atualizado_por varchar(255),
    atualizado_em timestamp without time zone
);

CREATE INDEX idx_plano_manutencao_preventiva_caminhao ON tb_plano_manutencao_preventiva (caminhao_id);

ALTER TABLE tb_manutencao
    ADD COLUMN IF NOT EXISTS km_odometro integer;

ALTER TABLE tb_manutencao
    ADD COLUMN IF NOT EXISTS plano_manutencao_preventiva_id uuid REFERENCES tb_plano_manutencao_preventiva (id);
