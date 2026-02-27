package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.dto.MotoristaSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.MotoristaSyncRequestProducer;
import br.com.frotasPro.api.service.motorista.MotoristaSyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegracaoMotoristaService {

    private final MotoristaSyncJobService jobService;
    private final MotoristaSyncRequestProducer producer;
    private final IntegracaoWinThorConfigService configService;

    public UUID solicitarSincronizacao(UUID empresaId) {
        return solicitarSincronizacao(empresaId, null);
    }

    public UUID solicitarSincronizacao(UUID empresaId, List<Integer> codigosMotoristas) {

        var job = jobService.criarJob(empresaId);
        List<Integer> codigosResolvidos = resolverCodigosMotoristas(empresaId, codigosMotoristas);

        MotoristaSyncRequestEvent event = MotoristaSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .codigosMotoristas(codigosResolvidos)
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

    private List<Integer> resolverCodigosMotoristas(UUID empresaId, List<Integer> codigosMotoristas) {
        if (codigosMotoristas != null) {
            return normalizarCodigos(codigosMotoristas);
        }

        var config = configService.getOrDefault(empresaId);
        if (config == null) {
            return null;
        }
        return normalizarCodigos(config.getCodigosMotoristas());
    }
}
