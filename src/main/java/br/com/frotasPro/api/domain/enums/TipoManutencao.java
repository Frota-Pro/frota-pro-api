package br.com.frotasPro.api.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.text.Normalizer;
import java.util.Locale;

@AllArgsConstructor
@Getter
@ToString
public enum TipoManutencao {
    PREVENTIVA("Manutenção preventiva"),
    CORRETIVA("Manutenção corretiva");

    private final String descricao;

    @JsonCreator
    public static TipoManutencao fromJson(String value) {
        if (value == null || value.isBlank()) return null;

        String normalized = normalize(value);

        for (TipoManutencao tipo : values()) {
            if (tipo.name().equals(normalized) || normalize(tipo.descricao).equals(normalized)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de manutenção inválido: " + value);
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
