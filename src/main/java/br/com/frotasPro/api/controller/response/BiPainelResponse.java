package br.com.frotasPro.api.controller.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Um dashboard do Metabase já com os dados de cada cartão, para o app desenhar sem WebView.
 * Os blocos vêm na ordem em que aparecem no dashboard (de cima para baixo, da esquerda para a direita).
 */
public record BiPainelResponse(
        int dashboardId,
        String titulo,
        Integer meses,
        LocalDate inicio,
        LocalDate fim,
        OffsetDateTime geradoEm,
        List<Bloco> blocos
) {

    /**
     * @param tipo    texto, scalar, bar, combo, line, area, row, pie, table ... (display do Metabase)
     * @param texto   só para tipo "texto" (markdown simples do dashboard)
     * @param erro    preenchido quando só este cartão falhou; o resto do painel segue normal
     * @param linhas  valores na ordem das colunas (número, texto ou nulo)
     * @param totalLinhas quantas linhas a consulta devolveu (as enviadas podem ser menos)
     */
    public record Bloco(
            String tipo,
            String titulo,
            String texto,
            String erro,
            List<Coluna> colunas,
            List<List<Object>> linhas,
            int totalLinhas
    ) {
    }

    /** tipo: numero, texto ou data. */
    public record Coluna(String nome, String rotulo, String tipo) {
    }
}
