package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.LadoPneu;
import br.com.frotasPro.api.domain.enums.PosicaoPneu;
import br.com.frotasPro.api.domain.enums.TipoTrocaPneu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrocaPneuManutencaoRequest {

    @NotBlank(message = "Pneu é obrigatório")
    @Size(max = 80, message = "Pneu inválido")
    private String pneu;

    @NotNull(message = "Número do eixo é obrigatório")
    private Integer eixoNumero;

    @NotNull(message = "Lado é obrigatório")
    private LadoPneu lado;

    @NotNull(message = "Posição é obrigatória")
    private PosicaoPneu posicao;

    @NotNull(message = "KM do odômetro é obrigatório")
    @PositiveOrZero(message = "KM do odômetro deve ser >= 0")
    private Integer kmOdometro;

    @NotNull(message = "Tipo de troca é obrigatório")
    private TipoTrocaPneu tipoTroca;
}
