package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
public class DespesaParadaRequest {

    @NotNull(message = "Parada é obrigatória")
    private UUID paradaId;

    @NotNull(message = "Tipo da despesa é obrigatório")
    private String tipoDespesa;

    @NotNull(message = "Data e hora são obrigatórias")
    private LocalDateTime dataHora;

    @NotNull(message = "Valor é obrigatório")
    private BigDecimal valor;

    private String descricao;
    private String cidade;
    private String uf;
}
