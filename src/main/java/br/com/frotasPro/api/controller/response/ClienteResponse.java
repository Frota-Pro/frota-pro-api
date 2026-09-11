package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ClienteResponse {
    private UUID id;
    private String documento;
    private String nome;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    private String telefone;
    private String email;
    private String codigoExterno;
    private LocalDateTime atualizadoEm;
}
