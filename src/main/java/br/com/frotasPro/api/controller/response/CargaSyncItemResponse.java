package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.integracao.CargaSyncItem;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Uma linha do histórico de sincronização de cargas. Peso em TONELADAS, valor em R$.
 * {@code tipo}: NOVA, ATUALIZADA, BLOQUEADA (diminuição retida) ou DESCARTADA.
 */
@Builder
public record CargaSyncItemResponse(
        UUID id,
        UUID jobId,
        String numeroCargaExterno,
        String tipo,
        String motivo,
        BigDecimal pesoAntes,
        BigDecimal pesoDepois,
        BigDecimal valorAntes,
        BigDecimal valorDepois,
        BigDecimal pesoWinthor,
        BigDecimal valorWinthor,
        String statusAntes,
        String statusDepois,
        String motoristaAntes,
        String motoristaDepois,
        String caminhaoAntes,
        String caminhaoDepois,
        LocalDate dtFaturamentoAntes,
        LocalDate dtFaturamentoDepois,
        Integer notasAntes,
        Integer notasDepois,
        LocalDateTime criadoEm
) {

    public static CargaSyncItemResponse de(CargaSyncItem i) {
        return CargaSyncItemResponse.builder()
                .id(i.getId())
                .jobId(i.getJobId())
                .numeroCargaExterno(i.getNumeroCargaExterno())
                .tipo(i.getTipo())
                .motivo(i.getMotivo())
                .pesoAntes(i.getPesoAntes())
                .pesoDepois(i.getPesoDepois())
                .valorAntes(i.getValorAntes())
                .valorDepois(i.getValorDepois())
                .pesoWinthor(i.getPesoWinthor())
                .valorWinthor(i.getValorWinthor())
                .statusAntes(i.getStatusAntes())
                .statusDepois(i.getStatusDepois())
                .motoristaAntes(i.getMotoristaAntes())
                .motoristaDepois(i.getMotoristaDepois())
                .caminhaoAntes(i.getCaminhaoAntes())
                .caminhaoDepois(i.getCaminhaoDepois())
                .dtFaturamentoAntes(i.getDtFaturamentoAntes())
                .dtFaturamentoDepois(i.getDtFaturamentoDepois())
                .notasAntes(i.getNotasAntes())
                .notasDepois(i.getNotasDepois())
                .criadoEm(i.getCriadoEm())
                .build();
    }
}
