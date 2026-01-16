package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaminhaoDetalheResponse {

    private CaminhaoResponse caminhao;

    private long totalCargas;
    private long cargasFinalizadas;

    private BigDecimal combustivelLitros;
    private BigDecimal combustivelValor;

    private BigDecimal pesoTransportado;

    private long ordensServicoAbertas;
}
