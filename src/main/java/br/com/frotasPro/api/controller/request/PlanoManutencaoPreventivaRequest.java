package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanoManutencaoPreventivaRequest {

    @NotBlank(message = "Caminhão é obrigatório")
    @Size(max = 80, message = "Caminhão inválido")
    private String caminhao;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 150, message = "Descrição deve ter no máximo 150 caracteres")
    private String descricao;

    @Min(value = 1, message = "Intervalo em KM deve ser maior que zero")
    private Integer intervaloKm;

    @Min(value = 1, message = "Intervalo em dias deve ser maior que zero")
    private Integer intervaloDias;

    private Boolean ativo;
}
