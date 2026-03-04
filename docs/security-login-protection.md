# Protecao de Login

## Objetivo
Reduzir risco de brute force e gerar trilha minima de auditoria para incidentes.

## Controles implementados

### Rate limit no `POST /login`
- Por IP: `frotapro.security.login.rate-limit.ip.max-attempts` em janela de `ip.window-seconds`
- Por usuario: `frotapro.security.login.rate-limit.user.max-attempts` em janela de `user.window-seconds`
- Quando excede: `429 Too Many Requests`

### Bloqueio progressivo por falhas
- Apos `first-threshold` falhas: bloqueio por `first-seconds`
- Apos `second-threshold` falhas: bloqueio por `second-seconds`
- Apos `third-threshold` falhas: bloqueio por `third-seconds`
- Chaves em Redis por IP e login.

### Auditoria minima
- `security_event=login_success`
- `security_event=login_failed`
- `security_event=password_changed`
- `security_event=password_changed_self`

## Metricas (Micrometer)
- `security_login_success_total`
- `security_login_rate_limited_total{dimension=ip|user}`
- `security_login_blocked_total`
