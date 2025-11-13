package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoParada {

    ABASTECIMENTO("Parada para Abastecimento"),
    PERNOITE("Parada para Dormir"),
    ALIMENTACAO("Parada de Almoço/Janta"),
    OUTROS("Outro tipo de parada");

    private final String descricao;

}
