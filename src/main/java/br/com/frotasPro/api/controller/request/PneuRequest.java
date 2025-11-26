package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.LadoPneu;
import br.com.frotasPro.api.domain.enums.PosicaoPneu;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PneuRequest {

    private String posicao;

    @NotNull(message = "Eixo é obrigatório")
    private UUID eixoId;

    private LadoPneu ladoAtual;
    private PosicaoPneu posicaoAtual;
}

