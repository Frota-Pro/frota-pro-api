package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.notificacao.NotificarVencimentosService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VencimentoScheduler {

    private final NotificarVencimentosService notificarVencimentosService;

    @Scheduled(cron = "0 0 6 * * *")
    public void notificarVencimentos() {
        notificarVencimentosService.notificarCnhVencendo();
        notificarVencimentosService.notificarDocumentosCaminhaoVencendo();
        notificarVencimentosService.notificarManutencoesPreventivasVencendo();
        notificarVencimentosService.notificarManutencoesEstagnadas();
    }
}
