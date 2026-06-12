package br.com.frotasPro.api.modules.integracao.kafka;

import br.com.frotasPro.api.modules.integracao.dto.response.MotoristaSyncResponseEvent;
import br.com.frotasPro.api.modules.logistica.service.MotoristaSyncJobService;
import br.com.frotasPro.api.modules.logistica.service.SincronizarMotoristaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MotoristaSyncResponseConsumer {

    private final SincronizarMotoristaService motoristaService;
    private final MotoristaSyncJobService jobService;

    @KafkaListener(
            topics = "${frotapro.kafka.topics.motorista-sync-response}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {
                    "spring.json.value.default.type=br.com.frotasPro.api.integracao.dto.MotoristaSyncResponseEvent"
            }
    )
    public void consumir(MotoristaSyncResponseEvent event) {
        log.info("📥 [API] Resposta de sync de motoristas recebida. jobId={} total={}",
                event.getJobId(),
                event.getMotoristas() != null ? event.getMotoristas().size() : 0);

        motoristaService.sincronizar(event);
        jobService.concluirJob(
                event.getJobId(),
                event.getMotoristas() != null ? event.getMotoristas().size() : 0
        );
    }
}

