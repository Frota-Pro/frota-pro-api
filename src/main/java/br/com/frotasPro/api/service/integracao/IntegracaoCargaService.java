package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.dto.CargaSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.CargaSyncRequestProducer;
import br.com.frotasPro.api.service.carga.CargaSyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegracaoCargaService {

    private final CargaSyncJobService jobService;
    private final CargaSyncRequestProducer producer;

    public UUID solicitarSincronizacao(UUID empresaId, LocalDate data) {

        var job = jobService.criarJob(empresaId, data);

        CargaSyncRequestEvent event = CargaSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .dataInicial(data)
                .dataFinal(data)
                .tipoCarga("FATURADA")
                .origem("API_FROTAPRO")
                .solicitadoPor("sistema")
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        producer.enviar(event);

        return job.getId();
    }
}
