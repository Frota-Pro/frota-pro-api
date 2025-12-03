package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.carga.CargaSyncJobService;
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

    @Value("${frotapro.empresa-sync-id}")
    private UUID empresaIdPadrao;

    @Scheduled(cron = "0 0 8-18/2 * * *")
    public void sincronizarCargasDiurnas() {
        log.info("Iniciando sincronização de cargas (período diurno)");
        sincronizarCargas();
    }

    @Scheduled(cron = "0 30 21,23 * * *")
    public void sincronizarCargasNoturnas() {
        log.info("Iniciando sincronização de cargas (período noturno)");
        sincronizarCargas();
    }

    private void sincronizarCargas() {
        LocalDate hoje = LocalDate.now();

        UUID jobId = cargaSyncJobService.solicitarSincronizacao(empresaIdPadrao, hoje);

        log.info("Job automático de sincronização de cargas disparado. jobId={} data={}",
                jobId, hoje);
    }
}


