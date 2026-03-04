# Deploy de Observabilidade e Alertas

## O que foi configurado
- Prometheus com scraping da API (`/actuator/prometheus`)
- Regras de alerta:
  - indisponibilidade da API
  - pico de erro `5xx`
  - pico de falha de login
- Alertmanager (rota default)
- Grafana com provisionamento automatico de:
  - datasource Prometheus
  - datasource Loki
  - dashboard `FrotaPro - Security & API Observability`

## Arquivos
- `docker-compose.monitoring.yaml`
- `monitoring/prometheus/prometheus.yml`
- `monitoring/prometheus/alerts.yml`
- `monitoring/alertmanager/alertmanager.yml`
- `monitoring/grafana/provisioning/datasources/datasource.yml`
- `monitoring/grafana/provisioning/dashboards/dashboards.yml`
- `monitoring/grafana/provisioning/dashboards/json/frotapro-security-observability.json`

## Pre-requisitos no servidor
1. A API precisa expor `/actuator/prometheus` para o Prometheus.
2. Se estiver com `frotapro.security.prometheus-permit-all=false`, habilite temporariamente para scraping:
   - no `.env` da API: `FROTAPRO_SECURITY_PROMETHEUS_PERMIT_ALL=true`
   - rede/firewall deve restringir acesso externo a essa rota.
3. Docker/Compose ativos.

## Subir stack de monitoramento
```bash
cd /home/frotapro/deploy/frotapro
docker compose -f docker-compose.monitoring.yaml up -d
docker compose -f docker-compose.monitoring.yaml ps
```

## Validacao rapida
1. Prometheus: `http://SEU_IP:9090`
2. Alertmanager: `http://SEU_IP:9093`
3. Grafana: `http://SEU_IP:3000` (admin/admin)
4. Em Prometheus > `Status > Targets`, o job `frota-pro-api` deve estar `UP`.
5. Em Grafana, verificar dashboard `FrotaPro - Security & API Observability`.

## Testar alertas
1. **API indisponivel**: pare a API por alguns minutos e verifique alerta `FrotaProApiDown`.
2. **Pico 5xx**: gerar chamadas que retornem erro 5xx e verificar `FrotaProApi5xxSpike`.
3. **Falha de login**: varias tentativas invalidas de login e verificar `FrotaProLoginFailureSpike`.
