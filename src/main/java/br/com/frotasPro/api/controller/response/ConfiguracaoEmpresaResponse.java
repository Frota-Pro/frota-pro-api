package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracaoEmpresaResponse {

    private String nomeEmpresa;
    private ArquivoResponse logo;
    private String emailRemetente;
    private String emailAssunto;
    private String emailCorpoHtml;
}
