package br.com.frotasPro.api.integracao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Resposta de GET https://brasilapi.com.br/api/cnpj/v1/{cnpj} — só os campos usados aqui. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrasilApiCnpjDto {
    private String razao_social;
    private String nome_fantasia;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String municipio;
    private String uf;
    private String cep;
    private String ddd_telefone_1;
}
