package br.com.frotasPro.api.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ParadaCargaResponse {

    private UUID id;
    private String Codigocarga;

    private String tipoParada;
    private LocalDateTime dtInicio;
    private LocalDateTime dtFim;

    private String cidade;
    private String local;
    private Integer kmOdometro;
    private String observacao;

    private List<DespesaParadaResponse> despesaParadas;
    //private List<AnexoParadaResponse> anexos;
    private ManutencaoResponse manutencao;
}
