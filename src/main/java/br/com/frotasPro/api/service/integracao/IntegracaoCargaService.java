package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.dto.CargaSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.CargaSyncRequestProducer;
import br.com.frotasPro.api.service.CargaSyncJobService;
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

        // cria job
        var job = jobService.criarJob(empresaId, data);

        // monta evento
        CargaSyncRequestEvent event = CargaSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .dataInicial(data)
                .dataFinal(data)
                .tipoCarga("FATURADA")
                .origem("API_FROTAPRO")
                .solicitadoPor("sistema") // depois você troca pelo usuário logado
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        // envia pro Kafka
        producer.enviar(event);

        return job.getId();
    }
}
