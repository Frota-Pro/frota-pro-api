alter table tb_carga
    add column if not exists transferencia_pendente boolean not null default false;

alter table tb_carga
    add column if not exists status_transferencia varchar(30) not null default 'SEM_TRANSFERENCIA';

create table if not exists tb_carga_transferencia (
    id uuid primary key,
    carga_origem_id uuid not null,
    carga_destino_id uuid,
    numero_carga_origem varchar(50) not null,
    numero_carga_destino varchar(50),
    numero_carga_externo_origem varchar(50),
    numero_carga_externo_destino varchar(50),
    status varchar(30) not null,
    total_notas integer,
    concluido_em timestamp,
    criado_por varchar(255),
    criado_em timestamp,
    atualizado_por varchar(255),
    atualizado_em timestamp,
    constraint fk_carga_transferencia_origem foreign key (carga_origem_id) references tb_carga(id),
    constraint fk_carga_transferencia_destino foreign key (carga_destino_id) references tb_carga(id)
);

create index if not exists ix_carga_transferencia_origem_status
    on tb_carga_transferencia (carga_origem_id, status);

create table if not exists tb_carga_transferencia_nota (
    id uuid primary key,
    transferencia_id uuid not null,
    cliente varchar(150) not null,
    nota varchar(30) not null,
    constraint fk_carga_transferencia_nota_transferencia
        foreign key (transferencia_id) references tb_carga_transferencia(id)
);

create index if not exists ix_carga_transferencia_nota_transferencia
    on tb_carga_transferencia_nota (transferencia_id);
