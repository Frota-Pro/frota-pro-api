package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoAnexoParada {
    FOTO_BOMBA_COMBUSTIVEL("Foto da bomba de combustivel"),
    CUPOM_COMBUSTIVEL("Cupom do abastecimento"),
    COMPROVANTE_PERNOITE("Comprovante de pernoite"),
    COMPROVANTE_ALIMENTACAO("Comprovante de Alimentação"),
    OUTRO("Outros");

    private final String descricao;
}
