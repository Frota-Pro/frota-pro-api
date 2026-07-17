package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteHistoricoRotaResponse {

    private String cliente;
    private Long quantidadeCargas;
    private LocalDate ultimaCargaEm;
}
