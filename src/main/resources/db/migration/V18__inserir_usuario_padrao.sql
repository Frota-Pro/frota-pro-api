INSERT INTO tb_usuario (
    id,
    nome,
    login,
    senha,
    criado_por,
    criado_em,
    atualizado_por,
    atualizado_em
)
VALUES (
    gen_random_uuid(),
    'Administrador do Sistema',
    'admin',
    '$2a$10$ADoK2kdZ8vRKhZMWquFy/eeRFrmzk/6QhzszhNdmxsy/3nc5Y7gEm',
    'Sistema',
    now(),
    'Sistema',
    now()
);

INSERT INTO tb_usuario_acesso (usuario_id, acesso_id)
SELECT u.id, a.id
FROM tb_usuario u
JOIN tb_acesso a ON a.nome = 'ROLE_ADMIN'
WHERE u.login = 'admin';

