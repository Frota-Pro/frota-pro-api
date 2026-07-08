package br.com.frotasPro.api.modules.manutencao.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.frotasPro.api.modules.logistica.dto.response.ParadaResumoResponse;
import br.com.frotasPro.api.shared.enums.StatusManutencao;
import br.com.frotasPro.api.shared.enums.TipoManutencao;

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
    private List<ManutencaoItemResponse> itens;

    private String observacoes;
    private BigDecimal valor;
    private StatusManutencao statusManutencao;

    private String codigoCaminhao;
    private String caminhao;

    private String codigoOficina;
    private String oficina;

    private ParadaResumoResponse parada;
    private List<TrocaPneuManutencaoResponse> trocasPneu;
}
