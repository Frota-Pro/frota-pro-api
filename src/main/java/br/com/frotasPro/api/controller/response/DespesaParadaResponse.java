package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DespesaParadaResponse {

    private UUID id;
    private UUID paradaId;
    private String tipoDespesa;
    private LocalDateTime dataHora;
    private BigDecimal valor;
    private String descricao;
    private String cidade;
    private String uf;
}
