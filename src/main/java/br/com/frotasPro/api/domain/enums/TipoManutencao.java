package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoManutencao {
    PREVENTIVA("Manutenção preventiva"),
    CORRETIVA("Manutenção corretiva");

    private final String descricao;
}
