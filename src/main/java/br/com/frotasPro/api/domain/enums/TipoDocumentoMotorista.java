package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoDocumentoMotorista {
    CNH("Carteira de Habilitação"),
    EXAME_MEDICO("Exame Medico"),
    CURSO_MOPP("Curso MOPP"),
    OUTRO("Outros");

    private final String descricao;
}
