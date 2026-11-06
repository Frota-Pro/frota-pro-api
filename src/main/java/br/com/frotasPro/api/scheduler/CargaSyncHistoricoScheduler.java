package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.carga.CargaSyncHistoricoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Apaga, todo dia, os itens do histórico de sincronização com mais de 90 dias. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CargaSyncHistoricoScheduler {

    private final CargaSyncHistoricoService historicoService;

    @Scheduled(cron = "0 45 6 * * *")
    public void purgarHistoricoAntigo() {
        try {
            historicoService.purgarAntigos();
        } catch (RuntimeException e) {
            log.warn("Não foi possível limpar o histórico antigo de sincronização: {}", e.getMessage());
        }
    }
}
