package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.TipoManutencao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ManutencaoParadaRequest {

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 3, max = 200, message = "Descrição deve ter entre 3 e 200 caracteres")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicioManutencao;

    private LocalDate dataFimManutencao;

    @NotNull(message = "Tipo de manutenção é obrigatório")
    private TipoManutencao tipoManutencao;

    private List<@Size(max = 120, message = "Item trocado inválido") String> itensTrocados;

    @Size(max = 500, message = "Observações deve ter no máximo 500 caracteres")
    private String observacoes;

    @DecimalMin(value = "0.00", message = "Valor deve ser >= 0")
    private BigDecimal valor;

    @NotNull(message = "Status da manutenção é obrigatório")
    private StatusManutencao statusManutencao;

    private UUID oficinaId;

    private List<@Valid TrocaPneuManutencaoRequest> trocasPneu;
}
