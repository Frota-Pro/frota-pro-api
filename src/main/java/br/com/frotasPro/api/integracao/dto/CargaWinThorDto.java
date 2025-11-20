package br.com.frotasPro.api.integracao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CargaWinThorDto {

    private Long numMdfe;
    private Integer numCar;

    private String codVeiculo;
    private Integer codMotorista;

    private OffsetDateTime dtSaida;
    private String destino;

    private Double pesoTotalKg;
    private String situacaoMdfe;

    private Integer totalClientes;
    private List<ClienteCargaWinThorDto> clientes;
}
