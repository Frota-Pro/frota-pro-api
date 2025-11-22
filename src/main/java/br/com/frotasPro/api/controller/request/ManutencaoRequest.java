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

@Getter @Setter
public class ManutencaoRequest {

    @NotBlank
    private String descricao;

    @NotNull
    private LocalDate dataInicioManutencao;

    private LocalDate dataFimManutencao;

    @NotNull
    private TipoManutencao tipoManutencao;

    private List<String> itensTrocados;

    private String observacoes;

    private BigDecimal valor;

    @NotNull
    private StatusManutencao statusManutencao;

    @NotNull
    private String caminhao;

    private String oficina;

}
