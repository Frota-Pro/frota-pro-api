package br.com.frotasPro.api.modules.logistica.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransferirNotasCargaRequest {

    @NotBlank(message = "Carga destino é obrigatória")
    private String numeroCargaDestino;

    @NotEmpty(message = "Informe ao menos uma nota para transferência")
    private List<@Valid NotaTransferenciaRequest> notas;

    @Getter
    @Setter
    public static class NotaTransferenciaRequest {

        @NotBlank(message = "Cliente da nota é obrigatório")
        private String cliente;

        @NotBlank(message = "Número da nota é obrigatório")
        private String nota;
    }
}
