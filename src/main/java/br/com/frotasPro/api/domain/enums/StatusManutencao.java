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
public enum StatusManutencao {
    AGENDADA("Manutenção Agendada"),
    EM_ANDAMENTO("Manutenção em andamento"),
    CONCLUIDA("Manutenção concluida"),
    CANCELADA("Manutenção cancelada");

    private final String descricao;

    @JsonCreator
    public static StatusManutencao fromJson(String value) {
        if (value == null || value.isBlank()) return null;

        String normalized = normalize(value);

        for (StatusManutencao status : values()) {
            if (status.name().equals(normalized) || normalize(status.descricao).equals(normalized)) {
                return status;
            }
        }

        return switch (normalized) {
            case "ABERTA" -> AGENDADA;
            case "EMANDAMENTO", "ANDAMENTO" -> EM_ANDAMENTO;
            case "FINALIZADA", "FECHADA", "ENCERRADA" -> CONCLUIDA;
            case "CANCELADO" -> CANCELADA;
            default -> throw new IllegalArgumentException("Status de manutenção inválido: " + value);
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
