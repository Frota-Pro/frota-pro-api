package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ClienteCargaResponse {

    private String cliente;
    private List<String> notas;
}
