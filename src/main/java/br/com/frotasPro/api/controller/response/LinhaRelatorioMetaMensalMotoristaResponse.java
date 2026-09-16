package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.TipoLinhaRelatorioMotorista;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinhaRelatorioMetaMensalMotoristaResponse {

    private LocalDate data;
    private String lote;
    private String cidade;
    private BigDecimal valorCarga;
    private BigDecimal tonelagem;

    private Integer kmInicial;
    private Integer kmFinal;
    private Long kmRodado;

    private BigDecimal litros;
    private BigDecimal valorAbastecimento;

    private BigDecimal mediaKmLitro;

    /** PROPRIA (padrão) ou uma das variantes de caminhão emprestado — ver o enum. */
    private TipoLinhaRelatorioMotorista tipoLinha;

    /**
     * Nome de quem efetivamente dirigiu essa carga — só preenchido quando
     * tipoLinha = MOTORISTA_TERCEIRO_NO_MEU_CAMINHAO (o motorista do
     * relatório é o titular do caminhão, não quem está nessa linha).
     */
    private String motoristaQueDirigiu;
}
