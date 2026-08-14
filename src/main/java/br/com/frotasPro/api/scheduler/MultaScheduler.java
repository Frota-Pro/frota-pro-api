package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.multa.NotificarPrazosMultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MultaScheduler {

    private final NotificarPrazosMultaService notificarPrazosMultaService;

    @Scheduled(cron = "0 0 6 * * *")
    public void notificarPrazosMultas() {
        notificarPrazosMultaService.processar();
    }
}
