package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioAbastecimentoPeriodoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioAbastecimentoPeriodoService {

    private final AbastecimentoRepository abastecimentoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;

    public RelatorioAbastecimentoPeriodoResponse gerar(LocalDate inicio, LocalDate fim, String codigoCaminhao, String codigoMotorista) {

        PeriodoValidator.obrigatorio(inicio, fim, "dtAbastecimento");

        Caminhao caminhao = null;
        Motorista motorista = null;

        if (codigoCaminhao != null && !codigoCaminhao.isBlank()) {
            caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(codigoCaminhao)
                    .orElseThrow(() -> new IllegalArgumentException("Caminhão não encontrado: " + codigoCaminhao));
        }
        if (codigoMotorista != null && !codigoMotorista.isBlank()) {
            motorista = motoristaRepository.findByCodigo(codigoMotorista)
                    .orElseThrow(() -> new IllegalArgumentException("Motorista não encontrado: " + codigoMotorista));
        }

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(LocalTime.MAX);

        List<Abastecimento> itens = abastecimentoRepository.findByPeriodoComFiltro(
                inicioDt, fimDt,
                caminhao == null ? null : caminhao.getId(),
                motorista == null ? null : motorista.getId()
        );

        Map<String, Integer> ultimoKmPorCaminhao = new HashMap<>();

        List<RelatorioAbastecimentoPeriodoResponse.Linha> linhas = itens.stream()
                .map(a -> {
                    String chaveCaminhao = a.getCaminhao() == null ? null : a.getCaminhao().getId().toString();
                    Integer kmAtual = a.getKmOdometro();

                    BigDecimal media = a.getMediaKmLitro();
                    if (media == null && chaveCaminhao != null && kmAtual != null && a.getQtLitros() != null
                            && a.getQtLitros().compareTo(BigDecimal.ZERO) > 0) {
                        Integer kmAnterior = ultimoKmPorCaminhao.get(chaveCaminhao);
                        if (kmAnterior != null) {
                            int kmRodado = kmAtual - kmAnterior;
                            if (kmRodado > 0) {
                                media = BigDecimal.valueOf(kmRodado).divide(a.getQtLitros(), 3, RoundingMode.HALF_UP);
                            }
                        }
                    }

                    if (chaveCaminhao != null && kmAtual != null) {
                        ultimoKmPorCaminhao.put(chaveCaminhao, kmAtual);
                    }

                    return RelatorioAbastecimentoPeriodoResponse.Linha.builder()
                            .data(LocalDate.from(a.getDtAbastecimento()))
                            .caminhao(a.getCaminhao() == null ? "-" : (a.getCaminhao().getPlaca() + " (" + a.getCaminhao().getCodigo() + ")"))
                            .motorista(a.getMotorista() == null ? "-" : (a.getMotorista().getNome() + " (" + a.getMotorista().getCodigo() + ")"))
                            .posto(a.getPosto())
                            .cidade(a.getCidade())
                            .litros(nvl(a.getQtLitros()))
                            .valorTotal(nvl(a.getValorTotal()))
                            .kmOdometro(kmAtual == null ? BigDecimal.ZERO : new BigDecimal(kmAtual))
                            .mediaKmLitro(nvl(media))
                            .tipoCombustivel(a.getTipoCombustivel() == null ? null : a.getTipoCombustivel().name())
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalLitros = itens.stream().map(Abastecimento::getQtLitros).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalValor = itens.stream().map(Abastecimento::getValorTotal).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);

        return RelatorioAbastecimentoPeriodoResponse.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .filtroCaminhao(caminhao == null ? "Todos" : (caminhao.getPlaca() + " (" + caminhao.getCodigo() + ")"))
                .filtroMotorista(motorista == null ? "Todos" : (motorista.getNome() + " (" + motorista.getCodigo() + ")"))
                .totalLitros(totalLitros)
                .totalValor(totalValor)
                .linhas(linhas)
                .build();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
