package br.com.frotasPro.api.modules.integracao.kafka;

import br.com.frotasPro.api.modules.integracao.dto.response.CargaSyncResponseEvent;
import br.com.frotasPro.api.modules.logistica.service.CargaSyncJobService;
import br.com.frotasPro.api.modules.logistica.service.SincronizarCargaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargaSyncResponseConsumer {

    private final SincronizarCargaService cargaService;
    private final CargaSyncJobService jobService;

    @KafkaListener(
            topics = "${frotapro.kafka.topics.carga-sync-response}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {
                    "spring.json.value.default.type=br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent"
            }
    )
    public void consumir(CargaSyncResponseEvent event) {
        log.info("📥 [API] Resposta de sync recebida. jobId={} totalCargas={}",
                event.getJobId(), event.getTotalCargas());

        try {
            jobService.marcarProcessando(event.getJobId());
            cargaService.sincronizarCargasWinThor(event);

            jobService.concluirJob(event.getJobId(), event.getTotalCargas());
            log.info("✅ [API] Job de sync concluído com sucesso. jobId={}", event.getJobId());
        } catch (Exception e) {
            log.error("❌ [API] Falha ao processar sync de cargas. jobId={} totalCargas={}",
                    event.getJobId(), event.getTotalCargas(), e);
            jobService.marcarErro(event.getJobId(), mensagemErro(e));
            throw e;
        }
    }

    private String mensagemErro(Exception ex) {
        String detalhe = ex.getMessage();
        if (detalhe == null || detalhe.isBlank()) {
            return "Falha ao processar resposta de sincronizacao de cargas.";
        }
        return "Falha ao processar resposta de sincronizacao de cargas: " + detalhe;
    }
}
