package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.kafka.CaminhaoSyncRequestProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarCaminhaoIntegracaoService {

    private final CaminhaoSyncRequestProducer requestProducer;

    @Transactional
    public void sincronizar(UUID empresaId) {
        UUID jobId = requestProducer.enviarPedidoSync(empresaId);
        log.info("Pedido de sync de caminhões disparado. jobId={}", jobId);
    }
}
