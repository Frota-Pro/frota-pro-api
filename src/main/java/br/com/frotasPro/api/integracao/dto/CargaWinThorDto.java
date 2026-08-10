package br.com.frotasPro.api.integracao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private BigDecimal valorTotal;

    private String situacaoMdfe;

    private Integer totalClientes;
    private List<ClienteCargaWinThorDto> clientes;

    /**
     * Códigos de devolução (CODDEVOL) encontrados pra notas desse carregamento
     * no WinThor. Vazio = nenhuma devolução associada.
     */
    private List<String> codigosDevolucao;

    /**
     * true se esse carregamento perdeu pedido(s) transferido(s) pra outro
     * carregamento no WinThor (PCLOGTRANSFNFCARREG).
     */
    private Boolean temTransferencia;
}
