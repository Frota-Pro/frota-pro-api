package br.com.frotasPro.api.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum StatusMeta {
    NAO_INICIADA("Meta não iniciada"),
    EM_ANDAMENTO("meta em andamento"),
    CONCLUIDA("meta concluida"),
    CANCELADA("meta cancelada");

    private final String descricao;

}
