package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioCargasSumidasWinThorResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Relatório gerencial das cargas que a reconciliação (VerificarCargasSumidasWinThorService)
 * marcou como não encontradas mais no WinThor — pra revisão manual do gestor.
 * Lista pequena por natureza (é uma exceção, não o volume normal de cargas),
 * então os filtros são aplicados em memória em vez de query dinâmica no banco.
 */
@Service
@RequiredArgsConstructor
public class RelatorioCargasSumidasWinThorService {

    private final CargaRepository cargaRepository;

    @Transactional(readOnly = true)
    public RelatorioCargasSumidasWinThorResponse gerar(LocalDate inicio, LocalDate fim,
                                                        String codigoMotorista, String codigoCaminhao) {
        List<Carga> cargas = cargaRepository.findByNaoEncontradaNoWinThorTrueOrderByDataVerificacaoWinThorDesc();

        List<RelatorioCargasSumidasWinThorResponse.Linha> linhas = cargas.stream()
                .filter(c -> dentroDoPeriodo(c, inicio, fim))
                .filter(c -> motoristaCombina(c, codigoMotorista))
                .filter(c -> caminhaoCombina(c, codigoCaminhao))
                .map(this::toLinha)
                .toList();

        return RelatorioCargasSumidasWinThorResponse.builder()
                .total((long) linhas.size())
                .linhas(linhas)
                .build();
    }

    private boolean dentroDoPeriodo(Carga carga, LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) {
            return true;
        }
        LocalDate referencia = carga.getDataVerificacaoWinThor() != null
                ? carga.getDataVerificacaoWinThor().toLocalDate()
                : null;
        if (referencia == null) {
            return false;
        }
        if (inicio != null && referencia.isBefore(inicio)) {
            return false;
        }
        return fim == null || !referencia.isAfter(fim);
    }

    private boolean motoristaCombina(Carga carga, String codigoMotorista) {
        if (codigoMotorista == null || codigoMotorista.isBlank()) {
            return true;
        }
        return carga.getMotorista() != null
                && codigoMotorista.trim().equalsIgnoreCase(carga.getMotorista().getCodigo());
    }

    private boolean caminhaoCombina(Carga carga, String codigoCaminhao) {
        if (codigoCaminhao == null || codigoCaminhao.isBlank()) {
            return true;
        }
        return carga.getCaminhao() != null
                && codigoCaminhao.trim().equalsIgnoreCase(carga.getCaminhao().getCodigo());
    }

    private RelatorioCargasSumidasWinThorResponse.Linha toLinha(Carga carga) {
        return RelatorioCargasSumidasWinThorResponse.Linha.builder()
                .numeroCarga(carga.getNumeroCarga())
                .numeroCargaExterno(carga.getNumeroCargaExterno())
                .statusCarga(carga.getStatusCarga().name())
                .dtSaida(carga.getDtSaida())
                .pesoCarga(carga.getPesoCarga())
                .valorTotal(carga.getValorTotal())
                .codigoMotorista(carga.getMotorista() != null ? carga.getMotorista().getCodigo() : null)
                .nomeMotorista(carga.getMotorista() != null ? carga.getMotorista().getNome() : null)
                .codigoCaminhao(carga.getCaminhao() != null ? carga.getCaminhao().getCodigo() : null)
                .placaCaminhao(carga.getCaminhao() != null ? carga.getCaminhao().getPlaca() : null)
                .codigoRota(carga.getRota() != null ? carga.getRota().getCodigo() : null)
                .dataVerificacaoWinThor(carga.getDataVerificacaoWinThor())
                .build();
    }
}
