package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CidadeResumoResponse {

    private String cidade;
    private Long quantidadeClientes;
    private Long quantidadeCargas;
}
