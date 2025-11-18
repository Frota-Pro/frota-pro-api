package br.com.frotasPro.api.integracao.kafka;

import br.com.frotasPro.api.integracao.dto.CargaSyncRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargaSyncRequestProducer {

    private final KafkaTemplate<String, CargaSyncRequestEvent> kafkaTemplate;

    @Value("${frotapro.kafka.topics.carga-sync-request}")
    private String topic;

    public void enviar(CargaSyncRequestEvent event) {
        log.info("📤 Enviando pedido de sync de cargas. jobId={} dataInicial={} dataFinal={}",
                event.getJobId(), event.getDataInicial(), event.getDataFinal());

        kafkaTemplate.send(topic, event.getJobId().toString(), event);
    }
}
