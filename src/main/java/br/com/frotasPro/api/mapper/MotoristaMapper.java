package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.domain.Motorista;

public class MotoristaMapper {
    public static MotoristaResponse toResponse(Motorista motorista){
        return MotoristaResponse.builder()
                .codigo(motorista.getCodigo())
                .nome(motorista.getNome())
                .email(motorista.getEmail())
                .dataNascimento(motorista.getDataNascimento())
                .cnh(motorista.getCnh())
                .validadeCnh(motorista.getValidadeCnh())
                .status(motorista.getStatus())
                .ativo(motorista.isAtivo())
                .build();
    }
}
