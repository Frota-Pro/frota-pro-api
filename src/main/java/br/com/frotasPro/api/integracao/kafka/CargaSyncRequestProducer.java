package br.com.frotasPro.api.integracao.kafka;

import br.com.frotasPro.api.integracao.dto.CargaSyncRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargaSyncRequestProducer {

    private final KafkaTemplate<String, CargaSyncRequestEvent> kafkaTemplate;

    @Value("${frotapro.kafka.topics.carga-sync-request}")
    private String topic;

    public CompletableFuture<SendResult<String, CargaSyncRequestEvent>> enviar(CargaSyncRequestEvent event) {
        log.info("📤 Enviando pedido de sync de cargas. jobId={} dataInicial={} dataFinal={} codigosCaminhoes={} codigosMotoristas={}",
                event.getJobId(), event.getDataInicial(), event.getDataFinal(),
                event.getCodigosCaminhoes(), event.getCodigosMotoristas());

        return kafkaTemplate.send(topic, event.getJobId().toString(), event);
    }
}
