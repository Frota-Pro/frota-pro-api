package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AbastecimentoResponse {

    private UUID id;
    private String codigo;

    private UUID paradaId;

    // em vez de UUID:
    private String caminhaoCodigo;
    private String caminhaoPlaca;

    private String motoristaCodigo;

    private LocalDateTime dtAbastecimento;
    private Integer kmOdometro;

    private BigDecimal qtLitros;
    private BigDecimal valorLitro;
    private BigDecimal valorTotal;
    private BigDecimal mediaKmLitro;

    private String tipoCombustivel;
    private String formaPagamento;

    private String posto;
    private String cidade;
    private String uf;

    private String numNotaOuCupom;
}
