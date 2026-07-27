package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasAtuadorResponse {

    private String statusGeral;

    @Builder.Default
    private Map<String, String> statusComponentes = Map.of();

    private Double uptimeSegundos;

    private Double memoriaUsadaMb;
    private Double memoriaMaximaMb;

    private BigDecimal cpuUsoPercentual;

    private Integer conexoesBancoAtivas;
    private Integer conexoesBancoMaximas;

    private long totalRequisicoesHttp;
}
