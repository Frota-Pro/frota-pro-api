package br.com.frotasPro.api.modules.integracao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteCargaWinThorDto {

    private Integer codCli;
    private String nomeCli;
    private List<Long> notas;
}
