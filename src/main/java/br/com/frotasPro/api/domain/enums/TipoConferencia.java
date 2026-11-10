package br.com.frotasPro.api.domain.enums;

import lombok.Getter;

/**
 * Tipos da tela de Conferência. Cada tipo é uma lista de itens que o sistema acha estranhos e que uma pessoa
 * confere; só depois de "Confirmar" o sistema muda alguma coisa. Para acrescentar um tipo novo: um valor aqui, o
 * serviço que monta a lista e os endpoints.
 */
@Getter
public enum TipoConferencia {

    REENTREGA_MOTORISTA_CAMINHAO(
            "Reentrega: motorista/caminhão diferente do manifesto",
            "Notas que voltaram de uma viagem e entraram num carregamento novo, com outro motorista ou caminhão. "
                    + "O MDF-e continua sendo o da viagem antiga, então a carga pode ter vindo com o motorista ou o "
                    + "caminhão errado.");

    private final String titulo;
    private final String descricao;

    TipoConferencia(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }
}
