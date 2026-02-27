package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.dto.CaminhaoSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.CaminhaoSyncRequestProducer;
import br.com.frotasPro.api.service.caminhao.CaminhaoSyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegracaoCaminhaoService {

    private final CaminhaoSyncJobService jobService;
    private final CaminhaoSyncRequestProducer producer;
    private final IntegracaoWinThorConfigService configService;

    public UUID solicitarSincronizacao(UUID empresaId, Integer codFilial) {
        return solicitarSincronizacao(empresaId, codFilial, null);
    }

    public UUID solicitarSincronizacao(UUID empresaId, Integer codFilial, List<Integer> codigosCaminhoes) {

        var job = jobService.criarJob(empresaId);

        List<Integer> codigosResolvidos = resolverCodigosCaminhoes(empresaId, codigosCaminhoes);

        CaminhaoSyncRequestEvent event = CaminhaoSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .codFilial(codFilial)
                .codigosCaminhoes(codigosResolvidos)
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        producer.enviar(event);

        return job.getId();
    }

    private List<Integer> normalizarCodigos(List<Integer> codigos) {
        if (codigos == null || codigos.isEmpty()) {
            return null;
        }
        return codigos;
    }

    private List<Integer> resolverCodigosCaminhoes(UUID empresaId, List<Integer> codigosCaminhoes) {
        if (codigosCaminhoes != null) {
            return normalizarCodigos(codigosCaminhoes);
        }

        var config = configService.getOrDefault(empresaId);
        if (config == null) {
            return null;
        }
        return normalizarCodigos(config.getCodigosCaminhoes());
    }
}
