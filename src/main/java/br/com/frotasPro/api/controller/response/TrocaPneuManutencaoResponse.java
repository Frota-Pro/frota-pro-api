package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.LadoPneu;
import br.com.frotasPro.api.domain.enums.PosicaoPneu;
import br.com.frotasPro.api.domain.enums.TipoTrocaPneu;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TrocaPneuManutencaoResponse {

    private UUID id;

    private String codigoManutencao;
    private String codigoCaminhao;

    private String codigoPneu;
    private Integer eixoNumero;
    private LadoPneu lado;
    private PosicaoPneu posicao;
    private Integer kmOdometro;
    private TipoTrocaPneu tipoTroca;
}
