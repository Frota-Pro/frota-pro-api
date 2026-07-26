package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioCargaCompletaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.DespesaParadaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioCargaCompletaService {

    private final CargaRepository cargaRepository;
    private final ParadaCargaRepository paradaCargaRepository;
    private final DespesaParadaRepository despesaParadaRepository;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    public RelatorioCargaCompletaResponse gerar(String numeroCarga) {

        Carga carga = cargaRepository.findByNumeroCargaWithNotas(numeroCarga)
                .orElseThrow(() -> new IllegalArgumentException("Carga não encontrada: " + numeroCarga));

        carga.setParadas(paradaCargaRepository.findAllByNumeroCargaOrderByDtFim(numeroCarga));

        List<RelatorioCargaCompletaResponse.Linha> linhas = new ArrayList<>();

        // NOTAS
        if (carga.getNotas() != null) {
            for (CargaNota n : carga.getNotas()) {
                linhas.add(RelatorioCargaCompletaResponse.Linha.builder()
                        .tipo("NOTA")
                        .data(carga.getDtChegada())
                        .descricao("Nota " + n.getNota() + " - Cliente " + n.getCliente())
                        .valor(null)
                        .km(null)
                        .build());
            }
        }

        // PARADAS
        if (carga.getParadas() != null) {
            for (ParadaCarga p : carga.getParadas()) {
                linhas.add(RelatorioCargaCompletaResponse.Linha.builder()
                        .tipo("PARADA")
                        .data(dataParada(p))
                        .descricao(descricaoParada(p))
                        .cidade(p.getCidade())
                        .valor(despesaParadaRepository.sumValorByParadaId(p.getId()))
                        .km(p.getKmOdometro() == null ? null : new BigDecimal(p.getKmOdometro()))
                        .build());
            }
        }

        linhas.sort(Comparator.comparing(RelatorioCargaCompletaResponse.Linha::getTipo)
                .thenComparing(RelatorioCargaCompletaResponse.Linha::getData, Comparator.nullsLast(Comparator.naturalOrder())));

        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();
        String numeroPrincipal = CargaMapper.resolverNumeroExibicao(
                carga.getNumeroCarga(), carga.getNumeroCargaExterno(), integracaoAtiva);
        String numeroSecundario = numeroPrincipal.equals(carga.getNumeroCarga())
                ? carga.getNumeroCargaExterno()
                : carga.getNumeroCarga();

        return RelatorioCargaCompletaResponse.builder()
                .numeroCarga(numeroPrincipal)
                .codigoCarga(numeroSecundario)
                .statusCarga(carga.getStatusCarga() == null ? null : carga.getStatusCarga().name())
                .motorista(carga.getMotorista() == null ? "-" : (carga.getMotorista().getNome() + " (" + carga.getMotorista().getCodigo() + ")"))
                .caminhao(carga.getCaminhao() == null ? "-" : (carga.getCaminhao().getPlaca() + " (" + carga.getCaminhao().getCodigo() + ")"))
                .rota(carga.getRota() == null ? "-" : (carga.getRota().getCodigo() + " - " + carga.getRota().getCidadeInicio()))
                .dataSaida(carga.getDtSaida())
                .dataChegada(carga.getDtChegada())
                .valorTotal(nvl(carga.getValorTotal()))
                .pesoCarga(nvl(carga.getPesoCarga()))
                .observacaoMotorista(carga.getObservacaoMotorista())
                .linhas(linhas)
                .build();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private LocalDate dataParada(ParadaCarga parada) {
        if (parada.getDtFim() != null) {
            return parada.getDtFim().toLocalDate();
        }
        return parada.getDtInicio() != null ? parada.getDtInicio().toLocalDate() : null;
    }

    private String descricaoParada(ParadaCarga parada) {
        String tipo = parada.getTipoParada() == null ? "Parada" : parada.getTipoParada().name();
        if (parada.getLocal() == null || parada.getLocal().isBlank()) {
            return tipo;
        }
        return tipo + " - " + parada.getLocal();
    }
}
