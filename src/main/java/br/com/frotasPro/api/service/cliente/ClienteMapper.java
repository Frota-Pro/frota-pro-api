package br.com.frotasPro.api.service.cliente;

import br.com.frotasPro.api.controller.response.ClienteResponse;
import br.com.frotasPro.api.domain.Cliente;

final class ClienteMapper {

    private ClienteMapper() {
    }

    static ClienteResponse toResponse(Cliente c) {
        return ClienteResponse.builder()
                .id(c.getId())
                .documento(c.getDocumento())
                .nome(c.getNome())
                .logradouro(c.getLogradouro())
                .numero(c.getNumero())
                .complemento(c.getComplemento())
                .bairro(c.getBairro())
                .cidade(c.getCidade())
                .uf(c.getUf())
                .cep(c.getCep())
                .telefone(c.getTelefone())
                .email(c.getEmail())
                .codigoExterno(c.getCodigoExterno())
                .atualizadoEm(c.getAtualizadoEm())
                .build();
    }
}
