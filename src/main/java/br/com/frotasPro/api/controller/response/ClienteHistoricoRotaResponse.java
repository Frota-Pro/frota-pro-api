package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteHistoricoRotaResponse {

    private String cliente;
    private String cidade;
    private Long quantidadeCargas;
    private LocalDate ultimaCargaEm;

    // Endereço do Cliente vinculado, quando já cadastrado — null enquanto a
    // nota ainda não foi enriquecida (sync do WinThor ou XML da nota).
    private String documento;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String uf;
    private String cep;
}
