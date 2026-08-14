package br.com.frotasPro.api.scheduler;

import br.com.frotasPro.api.service.carga.VerificarCargasSumidasWinThorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reconciliação periódica: cargas SINCRONIZADA (ainda não iniciadas) podem
 * ter sido apagadas/desvinculadas no WinThor depois de já terem sido
 * sincronizadas pro FrotaPRO. Roda a cada 3h — não precisa de mais
 * frequência que isso, é só um alerta pra revisão manual.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerificacaoCargaWinThorScheduler {

    private final VerificarCargasSumidasWinThorService verificarCargasSumidasWinThorService;

    @Scheduled(cron = "0 0 */3 * * *")
    public void verificarCargasSumidas() {
        try {
            verificarCargasSumidasWinThorService.verificar();
        } catch (Exception e) {
            log.error("Falha ao verificar cargas sumidas do WinThor.", e);
        }
    }
}
