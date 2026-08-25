package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResumoFiltroResponse;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Totais da tela de Abastecimentos — mesmos filtros da listagem paginada,
 * mas somando TODOS os registros que batem com o filtro, não só a página
 * carregada (era exatamente esse o bug: os cards mudavam de valor ao trocar
 * de página, porque calculavam em cima do array que estava na tela).
 */
@Service
@RequiredArgsConstructor
public class ResumoAbastecimentoFiltradoService {

    private final AbastecimentoRepository repository;

    public AbastecimentoResumoFiltroResponse resumir(
            String q,
            String caminhao,
            String motorista,
            TipoCombustivel tipo,
            FormaPagamento forma,
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        // Deixa preencher só "De" ou só "Até" — mesma regra do /filtrar, ver PeriodoParcialUtils.
        var periodo = PeriodoParcialUtils.abrirLadoAusente(inicio, fim);
        PeriodoValidator.opcional(periodo.inicio(), periodo.fim(), "dtAbastecimento");

        var row = repository.resumoFiltradoNative(
                norm(q),
                norm(caminhao),
                norm(motorista),
                tipo != null ? tipo.name() : null,
                forma != null ? forma.name() : null,
                periodo.inicio(),
                periodo.fim()
        );

        BigDecimal totalLitros = row.getTotalLitros() != null ? row.getTotalLitros() : BigDecimal.ZERO;
        BigDecimal totalValor = row.getTotalValor() != null ? row.getTotalValor() : BigDecimal.ZERO;
        BigDecimal somaMediaPonderada = row.getSomaMediaPonderada() != null ? row.getSomaMediaPonderada() : BigDecimal.ZERO;
        BigDecimal somaLitrosParaMedia = row.getSomaLitrosParaMedia() != null ? row.getSomaLitrosParaMedia() : BigDecimal.ZERO;

        BigDecimal precoMedioLitro = totalLitros.compareTo(BigDecimal.ZERO) > 0
                ? totalValor.divide(totalLitros, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal consumoMedioPonderado = somaLitrosParaMedia.compareTo(BigDecimal.ZERO) > 0
                ? somaMediaPonderada.divide(somaLitrosParaMedia, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AbastecimentoResumoFiltroResponse.builder()
                .totalLitros(totalLitros)
                .totalValor(totalValor)
                .precoMedioLitro(precoMedioLitro)
                .consumoMedioPonderado(consumoMedioPonderado)
                .totalRegistros(row.getTotalRegistros())
                .build();
    }

    private String norm(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
