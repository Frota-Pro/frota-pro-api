package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoMeta {
    QUILOMETRAGEM("Meta de quilometragem"),
    CONSUMO_COMBUSTIVEL("Meta de consumo de combustivel"),
    ENTREGA_PRAZO("Meta de entrega no prazo"),
    MANUTENCAO_PREVENTIVA("Meta manutençao preventiva"),
    CARGA_TRANSPORTADA("Meta de carga trasportada");

    private final String descricao;
}
