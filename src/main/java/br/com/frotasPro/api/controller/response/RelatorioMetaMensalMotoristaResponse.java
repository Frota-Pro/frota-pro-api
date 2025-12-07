package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioMetaMensalMotoristaResponse {

    private String nomeMotorista;
    private String codigoMotorista;
    private String placaCaminhao;
    private String codigoCaminhao;

    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private BigDecimal objetivoMesTonelada;
    private BigDecimal metaConsumoKmPorLitro;

    private List<LinhaRelatorioMetaMensalMotoristaResponse> linhas;

    private BigDecimal totalTonelada;
    private Long totalKmRodado;
    private BigDecimal totalLitros;
    private BigDecimal totalValorAbastecimento;
    private BigDecimal mediaGeralKmPorLitro;

    private BigDecimal realizadoToneladaPercentual;
}
