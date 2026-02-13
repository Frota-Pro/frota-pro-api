package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PneuMovimentacaoRequest {

    @NotBlank(message = "Tipo de movimentação é obrigatório")
    @Size(max = 30, message = "Tipo de movimentação inválido")
    public String tipo;            // TipoMovimentacaoPneu

    @DecimalMin(value = "0.00", message = "KM do evento deve ser >= 0")
    public BigDecimal kmEvento;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    public String observacao;

    public UUID caminhaoId;
    public UUID manutencaoId;
    public UUID paradaId;

    @Min(value = 1, message = "Número do eixo deve ser >= 1")
    public Integer eixoNumero;

    @Size(max = 20, message = "Lado inválido")
    public String lado;

    @Size(max = 20, message = "Posição inválida")
    public String posicao;

    // usado para INSTALACAO (obrigatório por regra de negócio)
    @DecimalMin(value = "0.00", message = "KM de instalação deve ser >= 0")
    public BigDecimal kmInstalacao;
}
