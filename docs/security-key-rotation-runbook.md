# Runbook de Rotacao de Chaves JWT

## Objetivo
Rotacionar o par de chaves JWT e invalidar todas as sessoes/tokens anteriores sem indisponibilizar a API.

## Pre-requisitos
- Acesso ao gerenciador de segredos do ambiente (Vault, Secrets Manager, Kubernetes Secret ou similar).
- Permissao para reiniciar a aplicacao.
- Janela de deploy aprovada.

## 1) Gerar novo par de chaves

### OpenSSL
```bash
openssl genpkey -algorithm RSA -out authz.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in authz.pem -out authz.pub
```

## 2) Publicar chaves no secret manager

- `JWT_PRIVATE_KEY`: caminho/valor da chave privada.
- `JWT_PUBLIC_KEY`: caminho/valor da chave publica.

Observacao:
- A aplicacao espera os valores em `jwt.private.key` e `jwt.public.key` via ambiente.
- Exemplo usando caminho de arquivo: `file:/run/secrets/authz.pem` e `file:/run/secrets/authz.pub`.

## 3) Atualizar deploy

- Garantir que as variaveis de ambiente estejam configuradas:
  - `JWT_PRIVATE_KEY`
  - `JWT_PUBLIC_KEY`
- Fazer rollout da aplicacao em todas as instancias.

## 4) Invalidar sessoes/tokens antigos

Como a assinatura muda, tokens emitidos com a chave antiga deixam de ser aceitos apos o rollout completo.

Checklist:
- Reiniciar todas as instancias da API.
- Confirmar que nao existe nenhuma instancia executando com chave antiga.
- Validar login novo e chamada autenticada.
- Validar que token antigo retorna `401`.

## 5) Pos-rotacao

- Remover arquivos locais temporarios (`authz.pem`, `authz.pub`) da maquina do operador.
- Registrar data/hora da rotacao e responsavel.
- Agendar proxima rotacao (recomendado trimestral ou semestral).

## Validacao rapida

1. Realizar login e capturar `accessToken`.
2. Chamar endpoint protegido com token novo: deve retornar `200`.
3. Reusar token antigo (gerado antes da rotacao): deve retornar `401`.

