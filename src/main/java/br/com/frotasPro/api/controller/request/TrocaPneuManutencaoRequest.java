package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.LadoPneu;
import br.com.frotasPro.api.domain.enums.PosicaoPneu;
import br.com.frotasPro.api.domain.enums.TipoTrocaPneu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrocaPneuManutencaoRequest {

    @NotBlank
    private String pneu;

    @NotNull
    private Integer eixoNumero;

    @NotNull
    private LadoPneu lado;

    @NotNull
    private PosicaoPneu posicao;

    @NotNull
    private Integer kmOdometro;

    @NotNull
    private TipoTrocaPneu tipoTroca;
}
