-- Duas travas de segurança de acesso que o administrador liga pela tela de Parâmetros do Sistema
-- (antes eram variáveis de ambiente). Nascem desligadas: ligar antes da hora trancaria usuários.
--  bloquear_senha_padrao : recusa o login com a senha fixa antiga "padrao123"
--  vincular_dispositivo  : motorista só entra pelo celular a que ficou vinculado (exige o app 1.8.0+)
ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS bloquear_senha_padrao boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS vincular_dispositivo  boolean NOT NULL DEFAULT false;
