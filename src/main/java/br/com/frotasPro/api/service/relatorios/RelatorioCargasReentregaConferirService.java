package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioCargasReentregaConferirResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.carga.CargaExistenciaWinThorClient;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * "Cantinho de conferência" das cargas de reentrega.
 *
 * No WinThor, uma reentrega fica "estacionada" num carregamento com motorista/veículo genéricos (REENTREGA) até a
 * nota ser faturada de novo e ir para uma viagem de verdade. A verificação de sumidas já tira do ar as cargas ainda
 * SINCRONIZADA nesse estado, mas uma carga que o motorista já INICIOU (ou finalizou) nunca é mexida sozinha: aparece
 * aqui para a pessoa conferir e decidir. Este relatório só lê; não altera nada.
 */
@Service
@RequiredArgsConstructor
public class RelatorioCargasReentregaConferirService {

    /** Quando nenhum período é informado, olha as finalizadas que saíram nos últimos N dias. */
    static final int DIAS_PADRAO = 30;

    private final CargaRepository cargaRepository;
    private final CargaExistenciaWinThorClient cargaExistenciaWinThorClient;

    @Transactional(readOnly = true)
    public RelatorioCargasReentregaConferirResponse gerar(LocalDate inicio, LocalDate fim) {
        LocalDate ate = fim != null ? fim : FusoHorarioUtils.hojeBrasil();
        LocalDate de = inicio != null ? inicio : ate.minusDays(DIAS_PADRAO);

        // Em rota: todas (estão acontecendo agora). Finalizadas: só as que saíram no período.
        List<Carga> candidatas = new ArrayList<>(
                cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNull(Status.EM_ROTA));
        candidatas.addAll(cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNullAndDtSaidaBetween(
                Status.FINALIZADA, de, ate));

        List<Integer> codigos = candidatas.stream()
                .map(c -> parseNumeroCargaExterno(c.getNumeroCargaExterno()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        Set<Integer> reentrega = codigos.isEmpty()
                ? Set.of()
                : cargaExistenciaWinThorClient.filtrarReentregaGenerica(codigos);

        List<RelatorioCargasReentregaConferirResponse.Linha> linhas = candidatas.stream()
                .filter(c -> {
                    Integer numCar = parseNumeroCargaExterno(c.getNumeroCargaExterno());
                    // Set imutável (Set.of/copyOf) lança NPE em contains(null).
                    return numCar != null && reentrega.contains(numCar);
                })
                // Em rota primeiro (é o que ainda dá para evitar), depois as mais recentes.
                .sorted(Comparator
                        .comparing((Carga c) -> c.getStatusCarga() != Status.EM_ROTA)
                        .thenComparing(Carga::getDtSaida, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toLinha)
                .toList();

        return RelatorioCargasReentregaConferirResponse.builder()
                .total((long) linhas.size())
                .linhas(linhas)
                .build();
    }

    private RelatorioCargasReentregaConferirResponse.Linha toLinha(Carga carga) {
        return RelatorioCargasReentregaConferirResponse.Linha.builder()
                .numeroCarga(carga.getNumeroCarga())
                .numeroCargaExterno(carga.getNumeroCargaExterno())
                .statusCarga(carga.getStatusCarga().name())
                .dtSaida(carga.getDtSaida())
                .pesoCarga(carga.getPesoCarga())
                .valorTotal(carga.getValorTotal())
                .kmInicial(carga.getKmInicial())
                .kmFinal(carga.getKmFinal())
                .codigoMotorista(carga.getMotorista() != null ? carga.getMotorista().getCodigo() : null)
                .nomeMotorista(carga.getMotorista() != null ? carga.getMotorista().getNome() : null)
                .codigoCaminhao(carga.getCaminhao() != null ? carga.getCaminhao().getCodigo() : null)
                .placaCaminhao(carga.getCaminhao() != null ? carga.getCaminhao().getPlaca() : null)
                .build();
    }

    private Integer parseNumeroCargaExterno(String numeroCargaExterno) {
        if (numeroCargaExterno == null || numeroCargaExterno.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(numeroCargaExterno.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
