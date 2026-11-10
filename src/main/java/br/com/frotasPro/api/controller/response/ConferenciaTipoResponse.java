package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConferenciaTipoResponse {

    /** Nome do tipo (usado no caminho da tela), ex.: REENTREGA_MOTORISTA_CAMINHAO. */
    private String codigo;
    private String titulo;
    private String descricao;
}
