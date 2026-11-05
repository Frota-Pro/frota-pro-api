package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.domain.integracao.IntegracaoWinThorConfig;
import br.com.frotasPro.api.service.integracao.IntegracaoCargaService;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargaScheduled {

    private static final LocalTime HORARIO_REFORCO_MENSAL_PADRAO = LocalTime.of(20, 0);

    private final IntegracaoCargaService integracaoCargaService;
    private final IntegracaoWinThorConfigService configService;
    private volatile LocalDateTime ultimoDisparoAutomatico;
    private volatile LocalDate ultimaExecucaoReforcoMensal;

    @Value("${frotapro.empresa-sync-id}")
    private UUID empresaIdPadrao;

    @Scheduled(cron = "0 * * * * *")
    public void sincronizarCargasAutomatico() {
        IntegracaoWinThorConfig cfg = configService.getOrDefault(empresaIdPadrao);

        // Compatibilidade: sem configuração, mantém comportamento anterior.
        Integer intervaloMin = cfg != null ? cfg.getIntervaloMin() : null;
        if (intervaloMin == null) {
            if (deveRodarNoCronLegado(FusoHorarioUtils.agoraBrasil())) {
                log.debug("Scheduler: execução no cron legado.");
                sincronizarCargas();
            }
            return;
        }

        if (intervaloMin < 1) {
            log.warn("Sincronização automática ignorada: intervaloMin inválido ({})", intervaloMin);
            return;
        }

        LocalDateTime agora = FusoHorarioUtils.agoraBrasil();
        if (ultimoDisparoAutomatico != null) {
            long minutos = Duration.between(ultimoDisparoAutomatico, agora).toMinutes();
            if (minutos < intervaloMin) {
                return;
            }
        }

        sincronizarCargas();
        ultimoDisparoAutomatico = agora;
    }

    private void sincronizarCargas() {
        IntegracaoWinThorConfig cfg = configService.getOrDefault(empresaIdPadrao);

        // compatibilidade: se não existir config, roda como antes
        boolean ativo = cfg == null || cfg.isAtivo();
        boolean syncCargas = cfg == null || cfg.isSyncCargas();

        if (!ativo || !syncCargas) {
            log.info("Sincronização automática de cargas DESATIVADA. ativo={} syncCargas={}", ativo, syncCargas);
            return;
        }

        LocalDate hoje = FusoHorarioUtils.hojeBrasil();
        UUID jobId = integracaoCargaService.solicitarSincronizacao(
                empresaIdPadrao,
                hoje,
                hoje,
                null,
                null,
                "FATURADA",
                "API_SCHEDULER",
                "SCHEDULER"
        );

        log.info("Job automático de sincronização de cargas disparado. jobId={} data={}", jobId, hoje);
    }

    /**
     * A sincronização automática de cima (sincronizarCargasAutomatico) só
     * busca o dia de hoje no WinThor — uma devolução ou transferência
     * lançada lá DEPOIS que uma carga de dias anteriores já foi
     * sincronizada nunca é pega automaticamente. Esse reforço roda uma vez
     * por dia (fora do horário comercial, por padrão às 20h — configurável
     * em Integrações > WinThor) cobrindo do dia 1 do mês até hoje, sem
     * interferir na sincronização por intervalo de cima.
     */
    @Scheduled(cron = "0 * * * * *")
    public void reforcoMensalAutomatico() {
        IntegracaoWinThorConfig cfg = configService.getOrDefault(empresaIdPadrao);

        boolean ativo = cfg == null || cfg.isAtivo();
        boolean syncCargas = cfg == null || cfg.isSyncCargas();
        if (!ativo || !syncCargas) {
            return;
        }

        LocalTime horario = cfg != null && cfg.getHorarioReforcoMensal() != null
                ? cfg.getHorarioReforcoMensal()
                : HORARIO_REFORCO_MENSAL_PADRAO;

        LocalDateTime agora = FusoHorarioUtils.agoraBrasil();
        LocalDate hoje = agora.toLocalDate();

        if (hoje.equals(ultimaExecucaoReforcoMensal) || agora.toLocalTime().isBefore(horario)) {
            return;
        }

        LocalDate primeiroDiaDoMes = hoje.withDayOfMonth(1);
        UUID jobId = integracaoCargaService.solicitarSincronizacao(
                empresaIdPadrao,
                primeiroDiaDoMes,
                hoje,
                null,
                null,
                "FATURADA",
                "API_SCHEDULER",
                "SCHEDULER_REFORCO_MENSAL"
        );

        ultimaExecucaoReforcoMensal = hoje;
        log.info("Job de reforço mensal (dia 1 até hoje) disparado. jobId={} periodo={} a {}",
                jobId, primeiroDiaDoMes, hoje);
    }

    private boolean deveRodarNoCronLegado(LocalDateTime agora) {
        int hora = agora.getHour();
        int minuto = agora.getMinute();

        boolean janelaDiurna = minuto == 0 && (hora == 8 || hora == 10 || hora == 12 || hora == 14 || hora == 16 || hora == 18);
        boolean janelaNoturna = minuto == 30 && (hora == 21 || hora == 23);
        return janelaDiurna || janelaNoturna;
    }
}
