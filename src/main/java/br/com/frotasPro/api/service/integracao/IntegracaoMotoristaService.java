package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.dto.MotoristaSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.MotoristaSyncRequestProducer;
import br.com.frotasPro.api.service.motorista.MotoristaSyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegracaoMotoristaService {

    private final MotoristaSyncJobService jobService;
    private final MotoristaSyncRequestProducer producer;

    public UUID solicitarSincronizacao(UUID empresaId) {

        var job = jobService.criarJob(empresaId);

        MotoristaSyncRequestEvent event = MotoristaSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        producer.enviar(event);

        return job.getId();
    }
}
