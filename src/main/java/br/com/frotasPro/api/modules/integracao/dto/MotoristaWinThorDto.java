package br.com.frotasPro.api.modules.integracao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotoristaWinThorDto {

    private Integer codigoExterno;
    private String nome;
    private String cpf;
    private Boolean ativo;
}
