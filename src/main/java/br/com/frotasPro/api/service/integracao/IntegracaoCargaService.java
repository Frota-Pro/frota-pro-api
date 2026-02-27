package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.integracao.dto.CargaSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.CargaSyncRequestProducer;
import br.com.frotasPro.api.service.carga.CargaSyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegracaoCargaService {

    private final CargaSyncJobService jobService;
    private final CargaSyncRequestProducer producer;
    private final IntegracaoWinThorConfigService configService;

    public UUID solicitarSincronizacao(UUID empresaId, LocalDate data) {
        return solicitarSincronizacao(empresaId, data, data, null, null, "FATURADA", "API_FROTAPRO", "sistema");
    }

    public UUID solicitarSincronizacao(
            UUID empresaId,
            LocalDate dataInicial,
            LocalDate dataFinal,
            List<Integer> codigosCaminhoes,
            List<Integer> codigosMotoristas,
            String tipoCarga,
            String origem,
            String solicitadoPor
    ) {

        LocalDate inicio = dataInicial != null ? dataInicial : LocalDate.now();
        LocalDate fim = dataFinal != null ? dataFinal : inicio;
        List<Integer> codigosCaminhoesResolvidos = resolverCodigosCaminhoes(empresaId, codigosCaminhoes);
        List<Integer> codigosMotoristasResolvidos = resolverCodigosMotoristas(empresaId, codigosMotoristas);

        var job = jobService.criarJob(empresaId, fim);

        CargaSyncRequestEvent event = CargaSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .dataInicial(inicio)
                .dataFinal(fim)
                .codigosCaminhoes(codigosCaminhoesResolvidos)
                .codigosMotoristas(codigosMotoristasResolvidos)
                .tipoCarga(tipoCarga)
                .origem(origem)
                .solicitadoPor(solicitadoPor)
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
