package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioCargasNaoSincronizadasResponse;
import br.com.frotasPro.api.domain.CargaSyncDescartada;
import br.com.frotasPro.api.domain.enums.MotivoCargaSyncDescartada;
import br.com.frotasPro.api.repository.CargaSyncDescartadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Cargas que vieram do WinThor mas o sync descartou porque o motorista ou o
 * caminhão do MDF-e não existe no FrotaPRO (ver SincronizarCargaService). A
 * lista é pequena por natureza (exceção, não o volume normal), então os
 * filtros rodam em memória.
 */
@Service
@RequiredArgsConstructor
public class RelatorioCargasNaoSincronizadasService {

    private final CargaSyncDescartadaRepository repository;

    @Transactional(readOnly = true)
    public RelatorioCargasNaoSincronizadasResponse gerar(LocalDate inicio, LocalDate fim, String motivo) {
        MotivoCargaSyncDescartada motivoFiltro = parseMotivo(motivo);

        List<CargaSyncDescartada> registros = repository.findAllByOrderByUltimaOcorrenciaEmDesc().stream()
                .filter(r -> dentroDoPeriodo(r, inicio, fim))
                .filter(r -> motivoFiltro == null || r.getMotivo() == motivoFiltro)
                .toList();

        long semMotorista = registros.stream()
                .filter(r -> r.getMotivo() != MotivoCargaSyncDescartada.CAMINHAO_NAO_CADASTRADO)
                .count();
        long semCaminhao = registros.stream()
                .filter(r -> r.getMotivo() != MotivoCargaSyncDescartada.MOTORISTA_NAO_CADASTRADO)
                .count();

        return RelatorioCargasNaoSincronizadasResponse.builder()
                .total((long) registros.size())
                .totalSemMotorista(semMotorista)
                .totalSemCaminhao(semCaminhao)
                .linhas(registros.stream().map(this::toLinha).toList())
                .build();
    }

    /** Filtra pela data de saída da carga (é o que o gestor lembra); sem data de saída, só aparece sem filtro. */
    private boolean dentroDoPeriodo(CargaSyncDescartada r, LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) {
            return true;
        }
        LocalDate saida = r.getDtSaida();
        if (saida == null) {
            return false;
        }
        if (inicio != null && saida.isBefore(inicio)) {
            return false;
        }
        return fim == null || !saida.isAfter(fim);
    }

    private MotivoCargaSyncDescartada parseMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return null;
        }
        try {
            return MotivoCargaSyncDescartada.valueOf(motivo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Motivo inválido: " + motivo);
        }
    }

    private RelatorioCargasNaoSincronizadasResponse.Linha toLinha(CargaSyncDescartada r) {
        return RelatorioCargasNaoSincronizadasResponse.Linha.builder()
                .numeroCargaExterno(r.getNumeroCargaExterno())
                .numMdfe(r.getNumMdfe())
                .situacaoMdfe(r.getSituacaoMdfe())
                .motivo(r.getMotivo().name())
                .motivoDescricao(r.getMotivo().getDescricao())
                .codigoMotoristaExterno(r.getCodigoMotoristaExterno())
                .nomeMotoristaExterno(r.getNomeMotoristaExterno())
                .codigoVeiculoExterno(r.getCodigoVeiculoExterno())
                .placaVeiculoExterno(r.getPlacaVeiculoExterno())
                .dtSaida(r.getDtSaida())
                .pesoCarga(r.getPesoCarga())
                .valorTotal(r.getValorTotal())
                .destino(r.getDestino())
                .totalClientes(r.getTotalClientes())
                .tentativas(r.getTentativas())
                .primeiraOcorrenciaEm(r.getPrimeiraOcorrenciaEm())
                .ultimaOcorrenciaEm(r.getUltimaOcorrenciaEm())
                .build();
    }
}
