ALTER TABLE tb_usuario
    ADD COLUMN nome VARCHAR(150) NOT NULL,
    ADD CONSTRAINT uk_usuario_nome UNIQUE (nome);
