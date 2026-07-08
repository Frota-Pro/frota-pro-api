package br.com.frotasPro.api.modules.manutencao.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

import br.com.frotasPro.api.shared.enums.LadoPneu;
import br.com.frotasPro.api.shared.enums.PosicaoPneu;
import br.com.frotasPro.api.shared.enums.TipoTrocaPneu;

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
