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

    public void enviar(CaminhaoSyncRequestEvent event) {

        log.info("📤 Enviando pedido de sync de caminhões. jobId={} empresaId={} codFilial={}",
                event.getJobId(), event.getEmpresaId(), event.getCodFilial());

        kafkaTemplate.send(topic, event.getJobId().toString(), event);
    }
}


