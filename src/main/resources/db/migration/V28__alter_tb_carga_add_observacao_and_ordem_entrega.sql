-- Observação do motorista na carga
alter table tb_carga
    add column if not exists observacao_motorista text;

-- Ordem de entrega (lista ordenada de clientes)
create table if not exists tb_carga_ordem_entrega (
                                                      carga_id uuid not null,
                                                      ordem integer not null,
                                                      cliente varchar(200) not null,
    constraint pk_tb_carga_ordem_entrega primary key (carga_id, ordem),
    constraint fk_tb_carga_ordem_entrega_carga foreign key (carga_id) references tb_carga(id)
    );

create index if not exists ix_tb_carga_ordem_entrega_carga_id
    on tb_carga_ordem_entrega (carga_id);