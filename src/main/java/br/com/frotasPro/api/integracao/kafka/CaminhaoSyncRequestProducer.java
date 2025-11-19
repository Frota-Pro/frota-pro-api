package br.com.frotasPro.api.integracao.kafka;

import br.com.frotasPro.api.integracao.dto.CaminhaoSyncRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaminhaoSyncRequestProducer {

    private final KafkaTemplate<String, CaminhaoSyncRequestEvent> kafkaTemplate;

    @Value("${frotapro.kafka.topics.caminhao-sync-request}")
    private String topic;

    public UUID enviarPedidoSync(UUID empresaId) {

        UUID jobId = UUID.randomUUID();

        CaminhaoSyncRequestEvent event = CaminhaoSyncRequestEvent.builder()
                .jobId(jobId)
                .empresaId(empresaId)
                .codFilial(null) // se quiser filtrar depois
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        kafkaTemplate.send(topic, jobId.toString(), event);

        log.info("📤 Enviando pedido de sync de caminhões. jobId={} empresaId={}", jobId, empresaId);

        return jobId;
    }
}
