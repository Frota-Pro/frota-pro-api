package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.carga.CargaSyncJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargaScheduled {

    private final CargaSyncJobService  cargaSyncJobService;

    @Scheduled(cron = "0 0 8,10,12,14,16,18 * * *")
    public void sincronizarCargasDiurnas() {
        log.info("Iniciando sincronização de cargas (período diurno)");
        sincronizarCargas();
    }

    @Scheduled(cron = "0 0 22 * * *")
    public void sincronizarCargasNoturnas() {
        log.info("Iniciando sincronização de cargas (período noturno)");
        sincronizarCargas();
    }

    private void sincronizarCargas() {
        cargaSyncJobService.criarJob(UUID.randomUUID(), LocalDate.now());
    }
}

