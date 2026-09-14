package br.com.frotasPro.api.integracao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteCargaWinThorDto {

    private Integer codCli;
    private String nomeCli;
    private String cidade;
    private List<Long> notas;

    // Cadastro do cliente no WinThor (pcclient), pra alimentar o Cliente
    // (CNPJ/CPF + endereço) automaticamente em toda sincronização — sem
    // depender de alguém abrir o XML da nota.
    private String documento;
    private String logradouro;
    private String numero;
    private String bairro;
    private String municipio;
    private String uf;
    private String cep;
}
