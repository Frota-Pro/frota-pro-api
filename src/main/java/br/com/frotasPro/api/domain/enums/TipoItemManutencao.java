package br.com.frotasPro.api.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.text.Normalizer;
import java.util.Locale;

public enum TipoItemManutencao {
    PECA,
    SERVICO;

    @JsonCreator
    public static TipoItemManutencao fromJson(String value) {
        if (value == null || value.isBlank()) return null;

        String normalized = normalize(value);
        return switch (normalized) {
            case "PECA", "PECAS" -> PECA;
            case "SERVICO", "SERVICOS" -> SERVICO;
            default -> throw new IllegalArgumentException("Tipo de item de manutenção inválido: " + value);
        };
    }

    private static String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_')
                .replaceAll("_+", "_");
    }
}
