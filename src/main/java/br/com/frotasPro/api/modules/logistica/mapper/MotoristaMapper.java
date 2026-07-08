package br.com.frotasPro.api.modules.logistica.mapper;

import br.com.frotasPro.api.modules.logistica.dto.response.MotoristaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;

public class MotoristaMapper {
    public static MotoristaResponse toResponse(Motorista motorista){
        return MotoristaResponse.builder()
                .id(motorista.getId())
                .codigo(motorista.getCodigo())
                .codigoExterno(motorista.getCodigoExterno())
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
