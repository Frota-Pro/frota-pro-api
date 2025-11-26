package br.com.frotasPro.api.controller.response;


import br.com.frotasPro.api.domain.enums.LadoPneu;
import br.com.frotasPro.api.domain.enums.PosicaoPneu;
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
    private String codigo;
    private String posicao;
    private LocalDate ultimaTroca;
    private Integer kmUltimaTroca;
    private UUID eixoId;

    private LadoPneu ladoAtual;
    private PosicaoPneu posicaoAtual;
}

