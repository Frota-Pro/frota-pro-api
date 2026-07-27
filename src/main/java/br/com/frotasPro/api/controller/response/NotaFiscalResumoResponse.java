package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaFiscalResumoResponse {
    private Long numeroNota;
    private String serie;

    /** E-mail cadastrado do cliente no WinThor, se houver — usado só pra pré-preencher o campo de envio. */
    private String emailCliente;
}
