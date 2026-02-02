package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.TipoItemManutencao;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

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
