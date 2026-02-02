package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.TipoManutencao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class ManutencaoRequest {

    @NotBlank
    private String descricao;

    @NotNull
    private LocalDate dataInicioManutencao;

    private LocalDate dataFimManutencao;

    @NotNull
    private TipoManutencao tipoManutencao;

    // Mantido por compatibilidade (antigo)
    private List<String> itensTrocados;

    private String observacoes;

    private BigDecimal valor;

    /**
     * Vínculo opcional com uma parada de carga.
     */
    private UUID paradaId;

    /**
     * Itens detalhados (peças/serviços). Se informado, o total da manutenção
     * poderá ser calculado a partir desses itens.
     */
    private List<ManutencaoItemRequest> itens;

    @NotNull
    private StatusManutencao statusManutencao;

    @NotNull
    private String caminhao;

    private String oficina;

    private List<TrocaPneuManutencaoRequest> trocasPneu;
}
