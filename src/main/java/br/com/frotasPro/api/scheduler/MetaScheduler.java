package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.meta.FechamentoAutomaticoMetaService;
import br.com.frotasPro.api.service.meta.ProcessarMetasDiariasService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetaScheduler {

    private final ProcessarMetasDiariasService processarMetasDiariasService;
    private final FechamentoAutomaticoMetaService fechamentoAutomaticoMetaService;

    @Scheduled(cron = "0 0 1 * * *")
    public void processarMetasDiarias() {
        processarMetasDiariasService.processar();
        // Depois do processamento: já com o mês novo criado, fecha o mês de quem bateu tudo.
        fechamentoAutomaticoMetaService.processarTodos();
    }
}
