package br.com.frotasPro.api.service.bi;

import com.fasterxml.jackson.databind.JsonNode;

/** Chamadas de incorporação do Metabase (a API do Metabase que aceita o link assinado, sem login). */
public interface MetabaseCliente {

    /** GET /api/embed/dashboard/{token}: nome, parâmetros e cartões do dashboard. */
    JsonNode dashboard(String token);

    /** GET /api/embed/dashboard/{token}/dashcard/{dashcardId}/card/{cardId}?parameters=...: dados do cartão. */
    JsonNode cartao(String token, int dashcardId, int cardId, String parametrosJson);
}
