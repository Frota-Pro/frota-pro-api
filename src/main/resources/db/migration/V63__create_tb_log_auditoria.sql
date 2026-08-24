CREATE TABLE tb_log_auditoria (
    id UUID PRIMARY KEY,
    data_hora TIMESTAMP NOT NULL,
    usuario_login VARCHAR(50),
    usuario_nome VARCHAR(150),
    acao VARCHAR(20) NOT NULL,
    entidade VARCHAR(80),
    descricao VARCHAR(255),
    metodo_http VARCHAR(10),
    endpoint VARCHAR(255),
    status_http INTEGER,
    ip VARCHAR(45)
);

-- consulta principal da tela de auditoria: período (sempre informado) + usuário (opcional)
CREATE INDEX idx_log_auditoria_data_hora ON tb_log_auditoria (data_hora DESC);
CREATE INDEX idx_log_auditoria_usuario_login ON tb_log_auditoria (usuario_login);
