package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum StatusManutencao {
    AGENDADA("Manutenção Agendada"),
    EM_ANDAMENTO("Manutenção em andamento"),
    CONCLUIDA("Manutenção concluida"),
    CANCELADA("Manutenção cancelada");

    private final String descricao;
}
