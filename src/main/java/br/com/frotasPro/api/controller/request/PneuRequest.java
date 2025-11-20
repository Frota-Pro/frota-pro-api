package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PneuRequest {

    private String posicao;
    private LocalDate ultimaTroca;

    @NotNull(message = "Eixo é obrigatório")
    private UUID eixoId;

}
