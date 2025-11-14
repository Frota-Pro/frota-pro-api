package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoDocumentoCaminhao {
    CRLV("CRLV"),
    CONTRATO_SEGURO("Contrato do Seguro"),
    VISTORIA("Documento da vistoria"),
    FOTO_FRENTE("Frente Caminhão"),
    FOTO_LATERAL("Lateral Caminhão"),
    FOTO_CHASSI("Chassi do caminhão"),
    OUTRO("Outros");

    private final String descricao;
}
