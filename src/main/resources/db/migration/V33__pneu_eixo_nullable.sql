-- Permitir pneu em ESTOQUE sem eixo
ALTER TABLE tb_pneu
    ALTER COLUMN eixo_id DROP NOT NULL;
