package br.com.frotasPro.api.controller.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PneuResponse {

    private UUID id;
    private String posicao;
    private LocalDate ultimaTroca;
    private UUID eixoId;
}
