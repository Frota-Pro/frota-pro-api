package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumoResponse {

    private long cargasAtivas;
    private long finalizadasHoje;

    @Builder.Default
    private BigDecimal litros30d = BigDecimal.ZERO;

    private long metasAtivas;
    private long osAbertas;

    @Builder.Default
    private List<DashboardCargaRecenteResponse> cargasRecentes = List.of();
}
