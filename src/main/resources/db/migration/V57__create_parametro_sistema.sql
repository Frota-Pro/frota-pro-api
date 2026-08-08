-- Regras de negócio hoje fixas no código (constantes das rotinas de
-- notificação), agora configuráveis pelo admin sem precisar de deploy.
CREATE TABLE IF NOT EXISTS tb_parametro_sistema (
    id                                        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id                                uuid    NOT NULL,

    dias_antecedencia_vencimento_documento    integer NOT NULL DEFAULT 5,
    km_antecedencia_troca_pneu                integer NOT NULL DEFAULT 500,
    dias_manutencao_estagnada                 integer NOT NULL DEFAULT 7,
    dias_antecedencia_prazo_multa             integer NOT NULL DEFAULT 5,

    criado_por      varchar(100),
    criado_em       timestamp without time zone,
    atualizado_por  varchar(100),
    atualizado_em   timestamp without time zone,

    CONSTRAINT uk_parametro_sistema_empresa UNIQUE (empresa_id)
);
