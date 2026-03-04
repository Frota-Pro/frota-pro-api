# Autenticacao com Refresh Token

## Configuracao
- Access token: `frotapro.security.jwt.access-token-seconds` (default `900` = 15 min)
- Refresh token: `frotapro.security.jwt.refresh-token-seconds` (default `604800` = 7 dias)
- Revogacao: blacklist em Redis por `jti` com TTL ate o vencimento do token.

## Endpoints

### `POST /login`
Autentica usuario e retorna novo par de tokens.

Request:
```json
{
  "login": "admin",
  "senha": "padrao123"
}
```

Response:
```json
{
  "accessToken": "...",
  "expiresIn": 900,
  "refreshToken": "...",
  "refreshExpiresIn": 604800
}
```

### `POST /login/refresh`
Rotaciona o refresh token e retorna novo par de tokens.

Request:
```json
{
  "refreshToken": "..."
}
```

Response:
```json
{
  "accessToken": "...",
  "expiresIn": 900,
  "refreshToken": "...",
  "refreshExpiresIn": 604800
}
```

Regras:
- aceita apenas token com `token_type=refresh`
- refresh token antigo e revogado no Redis
- token ja revogado retorna `401`

### `POST /login/logout`
Revoga o refresh token informado.

Request:
```json
{
  "refreshToken": "..."
}
```

Response:
- `204 No Content`

## Observacoes de seguranca
- Access token curto reduz janela de risco (15 min).
- Logout invalida refresh token; access token segue valido ate expirar.
- Rotacao de refresh evita reuso de token antigo.
