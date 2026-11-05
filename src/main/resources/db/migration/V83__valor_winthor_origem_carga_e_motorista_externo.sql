-- O que o WinThor diz HOJE sobre a carga (valor e peso das notas transportadas). Quando o bloqueio de
-- diminuição segura o valor_total/peso_carga num número maior, estes dois campos guardam o número real, e a
-- diferença entre eles é "quanto ia diminuir" (aparece na conferência do transportado).
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS valor_winthor numeric(15, 3);
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS peso_winthor  numeric(15, 3);

-- Como o carregamento nasceu, pelo log de transferência do WinThor: REENTREGA (pedido que voltou e ganhou
-- carga própria, em geral pequena) ou TRANSFERENCIA. origem_carga_numero = carregamento de onde veio.
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS origem_carga        varchar(20);
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS origem_carga_numero varchar(30);

-- Carga que ficou sem nota porque as notas foram para outras cargas (reentrega/transferência): o valor e o
-- peso são zerados dos totais, mas o que ela tinha antes e para onde foi fica guardado aqui.
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS transferida_para           varchar(300);
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS valor_antes_transferencia  numeric(15, 3);
ALTER TABLE tb_carga ADD COLUMN IF NOT EXISTS peso_antes_transferencia   numeric(15, 3);

-- Motorista de terceiro criado automaticamente pelo sync (manifesto TV10 de cliente de transporte terceirizado,
-- com caminhão da frota). Não tem acesso ao app: km e abastecimento da carga são lançados à mão.
ALTER TABLE tb_motorista ADD COLUMN IF NOT EXISTS externo boolean NOT NULL DEFAULT false;
