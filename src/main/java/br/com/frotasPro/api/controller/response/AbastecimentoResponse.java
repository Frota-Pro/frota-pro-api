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
    private UUID caminhaoId;
    private UUID motoristaId;

    private LocalDateTime dtAbastecimento;
    private Integer kmOdometro;

    private BigDecimal qtLitros;
    private BigDecimal valorLitro;
    private BigDecimal valorTotal;

    private String tipoCombustivel;
    private String formaPagamento;

    private String posto;
    private String cidade;
    private String uf;

    private String numNotaOuCupom;
}
