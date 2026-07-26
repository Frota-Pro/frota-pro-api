package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.TipoPlataformaDispositivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DispositivoAppRequest(
        @NotBlank(message = "Versão é obrigatória")
        @Size(max = 30, message = "Versão inválida")
        String versao,

        @NotNull(message = "Plataforma é obrigatória")
        TipoPlataformaDispositivo plataforma
) {
}
