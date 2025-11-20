package br.com.frotasPro.api.integracao.kafka;

import br.com.frotasPro.api.integracao.dto.MotoristaSyncRequestEvent;
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
public class MotoristaSyncRequestProducer {

    private final KafkaTemplate<String, MotoristaSyncRequestEvent> kafkaTemplate;

    @Value("${frotapro.kafka.topics.motorista-sync-request}")
    private String topic;

    public void enviar(MotoristaSyncRequestEvent event) {

        log.info("📤 Enviando pedido de sync de motoristas. jobId={} empresaId={}",
                event.getJobId(), event.getEmpresaId());

        kafkaTemplate.send(topic, event.getJobId().toString(), event);
    }
}


