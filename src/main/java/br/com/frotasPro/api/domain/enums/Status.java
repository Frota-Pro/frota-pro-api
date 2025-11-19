package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum Status {
    DISPONIVEL("Disponivel"),
    EM_ROTA("Em Rota"),
    SINCRONIZADA("Sincronizada");

    private final String descricao;

}
