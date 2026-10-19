package br.com.frotasPro.api.domain.enums;

public enum MotivoCargaSyncDescartada {

    MOTORISTA_NAO_CADASTRADO("Motorista não cadastrado no FrotaPRO"),
    CAMINHAO_NAO_CADASTRADO("Caminhão não cadastrado no FrotaPRO"),
    MOTORISTA_E_CAMINHAO_NAO_CADASTRADOS("Motorista e caminhão não cadastrados no FrotaPRO");

    private final String descricao;

    MotivoCargaSyncDescartada(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
