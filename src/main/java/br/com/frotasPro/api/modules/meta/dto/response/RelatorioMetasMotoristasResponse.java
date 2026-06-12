package br.com.frotasPro.api.modules.meta.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.frotasPro.api.shared.enums.TipoMeta;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioMetasMotoristasResponse {

    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private TipoMeta tipoMeta;
    private Long totalMotoristas;
    private Long totalDentroMeta;
    private Long totalForaMeta;
    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private String codigoMotorista;
        private String nomeMotorista;
        private BigDecimal meta;
        private BigDecimal realizado;
        private BigDecimal percentual;
        private String unidade;
        private Boolean dentroMeta;
        private String status;
    }
}
