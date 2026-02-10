package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.domain.integracao.IntegracaoWinThorConfig;
import br.com.frotasPro.api.service.carga.CargaSyncJobService;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargaScheduled {

    private final CargaSyncJobService cargaSyncJobService;
    private final IntegracaoWinThorConfigService configService;

    @Value("${frotapro.empresa-sync-id}")
    private UUID empresaIdPadrao;

    @Scheduled(cron = "0 0 8-18/2 * * *")
    public void sincronizarCargasDiurnas() {
        log.debug("Scheduler: janela diurna");
        sincronizarCargas();
    }

    @Scheduled(cron = "0 30 21,23 * * *")
    public void sincronizarCargasNoturnas() {
        log.debug("Scheduler: janela noturna");
        sincronizarCargas();
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

        LocalDate hoje = LocalDate.now();
        UUID jobId = cargaSyncJobService.solicitarSincronizacao(empresaIdPadrao, hoje);

        log.info("Job automático de sincronização de cargas disparado. jobId={} data={}", jobId, hoje);
    }
}
