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

    /** Só informativos (relatório de cargas descartadas no sync) — o vínculo é sempre pelo código. */
    private String nomeMotorista;
    private String placaVeiculo;

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

    /** "REENTREGA", "TRANSFERENCIA" ou nulo (carga comum): como o carregamento nasceu, pelo log do WinThor. */
    private String origemCarga;
    /** Carregamento de onde veio o primeiro pedido. */
    private Integer origemNumCar;

    /** Quantas notas do carregamento são transporte (não canceladas e com MDF-e). */
    private Integer notasTransportadas;
    /** Todas as notas transportadas são TV10 de cliente de transporte terceirizado (15575, 10073). */
    private Boolean somenteTv10Terceiro;
}
