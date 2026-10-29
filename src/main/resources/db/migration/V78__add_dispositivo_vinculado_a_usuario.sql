-- Vínculo do motorista com UM celular: o primeiro login grava a identificação do aparelho e
-- logins de outro aparelho são recusados até um administrador desvincular.
-- Guarda só o hash do identificador (nunca o valor bruto) e o nome do modelo, para a tela do admin.
ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS dispositivo_vinculado_hash varchar(64),
    ADD COLUMN IF NOT EXISTS dispositivo_vinculado_nome varchar(120),
    ADD COLUMN IF NOT EXISTS dispositivo_vinculado_em   timestamp without time zone;
