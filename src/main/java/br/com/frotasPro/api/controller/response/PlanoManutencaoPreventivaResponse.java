package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanoManutencaoPreventivaResponse {

    private UUID id;
    private String codigoCaminhao;
    private String caminhao;

    private String descricao;
    private Integer intervaloKm;
    private Integer intervaloDias;
    private boolean ativo;

    private Integer ultimoKmExecutado;
    private LocalDate ultimaDataExecutada;

    /** Odômetro atual do caminhão (última carga finalizada) — contexto pra saber o quão perto está do vencimento. */
    private Integer odometroAtualCaminhao;

    private Integer proximoKm;
    private LocalDate proximaData;
}
