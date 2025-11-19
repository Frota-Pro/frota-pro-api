package br.com.frotasPro.api.integracao.kafka;

import br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent;
import br.com.frotasPro.api.service.carga.CargaSyncJobService;
import br.com.frotasPro.api.service.carga.SincronizarCargaService;
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

        cargaService.sincronizarCargasWinThor(event);
        jobService.concluirJob(event.getJobId(), event.getTotalCargas());
    }
}

