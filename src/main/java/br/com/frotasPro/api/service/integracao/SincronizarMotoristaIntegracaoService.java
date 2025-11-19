package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.kafka.MotoristaSyncRequestProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarMotoristaIntegracaoService {

    private final MotoristaSyncRequestProducer producer;

    public void dispararSincronizacao(UUID empresaId) {
        UUID jobId = producer.enviar(empresaId);
        log.info("Pedido de sync de motoristas disparado. jobId={}", jobId);
    }
}
