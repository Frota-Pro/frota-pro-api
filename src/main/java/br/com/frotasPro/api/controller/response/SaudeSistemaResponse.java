package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaudeSistemaResponse {

    // Adoção do app
    private int totalMotoristasComUsuario;
    private int motoristasAtivosUltimos7Dias;
    private int motoristasAtivosUltimos30Dias;
    private int motoristasNuncaAcessaram;
    private long totalAcessosAcumulado;

    @Builder.Default
    private List<MotoristaAcessoResponse> motoristas = List.of();

    // Atrasos no período
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private long totalCargasFinalizadasPeriodo;

    private long cargasComAtrasoInicio;
    private long cargasComAtrasoFim;

    private BigDecimal percentualAtrasoInicio;
    private BigDecimal percentualAtrasoFim;

    private BigDecimal atrasoMedioInicioDias;
    private BigDecimal atrasoMedioFimDias;

    @Builder.Default
    private List<MotoristaAtrasoResponse> rankingAtrasoMotoristas = List.of();
}
