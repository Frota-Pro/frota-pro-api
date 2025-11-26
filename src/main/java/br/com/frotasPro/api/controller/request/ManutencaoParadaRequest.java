package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.TipoManutencao;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ManutencaoParadaRequest {
    private String descricao;
    private LocalDate dataInicioManutencao;
    private LocalDate dataFimManutencao;
    private TipoManutencao tipoManutencao;
    private List<String> itensTrocados;
    private String observacoes;
    private BigDecimal valor;
    private StatusManutencao statusManutencao;
    private UUID oficinaId;
    private List<TrocaPneuManutencaoRequest> trocasPneu;
}