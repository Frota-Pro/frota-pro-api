create table if not exists tb_integracao_winthor_config (
    id uuid primary key default gen_random_uuid(),
    empresa_id uuid not null,

    ativo boolean not null default true,
    intervalo_min integer,

    sync_caminhoes boolean not null default true,
    sync_motoristas boolean not null default true,
    sync_cargas boolean not null default true,

    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),

    constraint uk_integracao_winthor_config_empresa unique (empresa_id)
    );

create index if not exists ix_integracao_winthor_config_ativo
    on tb_integracao_winthor_config (ativo);
