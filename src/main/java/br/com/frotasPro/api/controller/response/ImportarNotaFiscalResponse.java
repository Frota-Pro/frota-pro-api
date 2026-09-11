package br.com.frotasPro.api.controller.response;

import lombok.Builder;

import java.util.List;

/**
 * Resultado de um upload de XML(s) de NFe pra uma carga — separa o que foi
 * importado de fato do que já existia (mesmo cliente + mesmo número de nota),
 * pra deixar claro pro usuário quando algum XML enviado era duplicado.
 */
@Builder
public record ImportarNotaFiscalResponse(
        CargaResponse carga,
        int notasNovas,
        /** "Cliente — nota X", uma por nota que já existia e foi ignorada (só o arquivo pode ter sido vinculado). */
        List<String> notasJaExistentes
) {
}
