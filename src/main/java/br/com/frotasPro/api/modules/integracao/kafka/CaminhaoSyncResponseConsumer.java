package br.com.frotasPro.api.modules.integracao.kafka;

import br.com.frotasPro.api.modules.frota.service.CaminhaoSyncJobService;
import br.com.frotasPro.api.modules.frota.service.SincronizarCaminhaoService;
import br.com.frotasPro.api.modules.integracao.dto.response.CaminhaoSyncResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaminhaoSyncResponseConsumer {

    private final SincronizarCaminhaoService caminhaoService;
    private final CaminhaoSyncJobService jobService;

    @KafkaListener(
            topics = "${frotapro.kafka.topics.caminhao-sync-response}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {
                    "spring.json.value.default.type=br.com.frotasPro.api.integracao.dto.CaminhaoSyncResponseEvent"
            }
    )
    public void consumir(CaminhaoSyncResponseEvent event) {
        log.info("📥 [API] Resposta de sync de caminhões recebida. jobId={} total={}",
                event.getJobId(),
                event.getCaminhoes() != null ? event.getCaminhoes().size() : 0);

        caminhaoService.sincronizar(event);
        jobService.concluirJob(
                event.getJobId(),
                event.getCaminhoes() != null ? event.getCaminhoes().size() : 0
        );
    }
}

