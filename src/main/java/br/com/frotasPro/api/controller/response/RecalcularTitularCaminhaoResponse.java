package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecalcularTitularCaminhaoResponse {
    private String codigoCaminhao;
    private String motoristaTitularCodigo;
    private String motoristaTitularNome;
    private LocalDate dataInicio;
    private int cargasAtualizadas;
    private int abastecimentosAtualizados;
}
