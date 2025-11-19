package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.dto.CaminhaoSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.CaminhaoSyncRequestProducer;
import br.com.frotasPro.api.service.caminhao.CaminhaoSyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegracaoCaminhaoService {

    private final CaminhaoSyncJobService jobService;
    private final CaminhaoSyncRequestProducer producer;

    public UUID solicitarSincronizacao(UUID empresaId, Integer codFilial) {

        var job = jobService.criarJob(empresaId);

        CaminhaoSyncRequestEvent event = CaminhaoSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .codFilial(codFilial)
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        producer.enviar(event);

        return job.getId();
    }
}
