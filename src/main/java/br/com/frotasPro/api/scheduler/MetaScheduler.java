package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.meta.ProcessarMetasDiariasService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetaScheduler {

    private final ProcessarMetasDiariasService processarMetasDiariasService;

    @Scheduled(cron = "0 0 1 * * *")
    public void processarMetasDiarias() {
        processarMetasDiariasService.processar();
    }
}
