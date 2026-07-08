package br.com.frotasPro.api.modules.manutencao.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.frotasPro.api.shared.enums.TipoItemManutencao;

@Getter
@Setter
public class ManutencaoItemResponse {

    private UUID id;
    private TipoItemManutencao tipo;
    private String descricao;
    private BigDecimal quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
}
