-- Fechamento automático da meta mensal: quando o motorista já bateu TODAS as metas do caminhão dele
-- no mês, o sistema fecha o mês por ele (sem esperar ele confirmar no app). Nasce desligado: o
-- deploy não muda nada até o administrador ligar na tela de Parâmetros do Sistema.
ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS fechar_meta_automaticamente boolean NOT NULL DEFAULT false;
