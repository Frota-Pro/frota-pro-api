ALTER TABLE tb_manutencao
    ADD COLUMN IF NOT EXISTS valor_orcado numeric(12, 2);

-- Manutenções já existentes são consideradas aprovadas (não retroage o bloqueio novo).
ALTER TABLE tb_manutencao
    ADD COLUMN IF NOT EXISTS status_aprovacao varchar(20) NOT NULL DEFAULT 'APROVADO';

ALTER TABLE tb_manutencao
    ADD COLUMN IF NOT EXISTS observacao_aprovacao varchar(500);
