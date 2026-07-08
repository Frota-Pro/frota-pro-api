package br.com.frotasPro.api.modules.manutencao.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.frotasPro.api.modules.meta.dto.response.SerieMensalValorResponse;
import br.com.frotasPro.api.modules.relatorio.dto.response.TopCaminhaoCustoResponse;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OficinaDashboardResponse {

    private String codigoOficina;
    private String nomeOficina;

    private LocalDate inicio;
    private LocalDate fim;

    // KPIs
    private BigDecimal totalOrcamentos;
    private Long qtdManutencoes;
    private BigDecimal ticketMedio;

    private Long qtdAgendadas;
    private Long qtdEmAndamento;
    private Long qtdConcluidas;
    private Long qtdCanceladas;

    private BigDecimal totalPecas;
    private BigDecimal totalServicos;

    // Gráficos
    private List<SerieMensalValorResponse> serieMensal;
    private List<TopCaminhaoCustoResponse> topCaminhoes;
}
