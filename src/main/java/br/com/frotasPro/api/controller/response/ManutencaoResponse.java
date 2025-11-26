package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.TipoManutencao;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManutencaoResponse {

    private UUID id;
    private String codigo;
    private String descricao;
    private LocalDate dataInicioManutencao;
    private LocalDate dataFimManutencao;
    private TipoManutencao tipoManutencao;
    private List<String> itensTrocados;
    private String observacoes;
    private BigDecimal valor;
    private StatusManutencao statusManutencao;
    private String codigoCaminhao;
    private String caminhao;
    private String codigoOficina;
    private String oficina;
    private List<TrocaPneuManutencaoResponse> trocasPneu;
}
