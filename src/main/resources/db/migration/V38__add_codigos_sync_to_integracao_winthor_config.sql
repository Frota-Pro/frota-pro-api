alter table tb_integracao_winthor_config
    add column if not exists codigos_caminhoes text;

alter table tb_integracao_winthor_config
    add column if not exists codigos_motoristas text;
