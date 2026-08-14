package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.GravidadeMulta;
import br.com.frotasPro.api.domain.enums.ResponsavelPagamentoMulta;
import br.com.frotasPro.api.domain.enums.StatusPagamentoMulta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultaResponse {

    private UUID id;

    private String codigoCaminhao;
    private String caminhao;

    private String codigoMotorista;
    private String motorista;

    private LocalDate dataInfracao;
    private String orgaoAutuador;
    private String numeroAit;
    private String descricaoInfracao;
    private GravidadeMulta gravidade;
    private Integer pontos;
    private BigDecimal valor;

    private LocalDate dataVencimentoPagamento;
    private LocalDate dataLimiteRecurso;

    private StatusPagamentoMulta statusPagamento;
    private ResponsavelPagamentoMulta responsavelPagamento;

    private String observacao;
}
