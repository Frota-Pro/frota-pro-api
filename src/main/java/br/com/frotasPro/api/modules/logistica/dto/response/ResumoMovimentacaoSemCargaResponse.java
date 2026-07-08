package br.com.frotasPro.api.modules.logistica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumoMovimentacaoSemCargaResponse {

    private String codigoCaminhao;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private Long totalKmRodado;
    private BigDecimal custoEstimadoTotal;
}
