package br.com.frotasPro.api.integracao.dto;

import lombok.Builder;

import java.math.BigDecimal;

/** Dados extraídos do XML de uma NFe (modelo 55), já emitida em outro sistema. */
@Builder
public record NotaFiscalXmlDto(
        String numeroNota,
        String nomeCliente,
        String cidadeCliente,
        BigDecimal pesoBruto,
        BigDecimal valorTotal,

        /** CNPJ ou CPF do destinatário, só dígitos — chave natural do cliente. */
        String documentoCliente,
        String logradouroCliente,
        String numeroCliente,
        String complementoCliente,
        String bairroCliente,
        String ufCliente,
        String cepCliente,
        String telefoneCliente,
        String emailCliente
) {
}
