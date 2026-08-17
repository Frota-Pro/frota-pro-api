package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.auditoria.PurgarLogAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditoriaScheduler {

    private final PurgarLogAuditoriaService purgarLogAuditoriaService;

    @Scheduled(cron = "0 30 6 * * *")
    public void purgarAuditoriaAntiga() {
        purgarLogAuditoriaService.purgarAntigos();
    }
}
