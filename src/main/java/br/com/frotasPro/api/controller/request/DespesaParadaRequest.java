package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DespesaParadaRequest {

    @NotNull(message = "Parada é obrigatória")
    private UUID paradaId;

    @NotBlank(message = "Tipo da despesa é obrigatório")
    @Size(max = 60, message = "Tipo da despesa inválido")
    private String tipoDespesa;

    @NotNull(message = "Data e hora são obrigatórias")
    private LocalDateTime dataHora;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
    private String cidade;

    @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF inválida (ex: PB)")
    private String uf;
}
