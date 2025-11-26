package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VidaUtilPneuResponse {

    private String codigoPneu;
    private String codigoCaminhao;
    private Integer eixoNumero;
    private String lado;
    private String posicao;

    private Integer kmInstalacao;
    private Integer kmRemocao;
    private Integer kmRodados;
}
