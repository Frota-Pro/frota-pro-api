package br.com.frotasPro.api.excption;

import java.time.Instant;
import java.util.List;

/**
 * Corpo da resposta 409 de {@link AbastecimentoRequerConfirmacaoException} —
 * igual a CustomException, mas com a lista de avisos em vez de uma mensagem
 * só, pra tela conseguir listar cada um.
 */
public record AbastecimentoAvisoResponse(Instant timestamp, Integer status, List<String> avisos, String path) {
}
