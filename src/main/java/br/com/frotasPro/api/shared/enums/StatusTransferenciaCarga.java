package br.com.frotasPro.api.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusTransferenciaCarga {
    SEM_TRANSFERENCIA("Sem transferencia"),
    PENDENTE_SYNC("Pendente de sincronizacao"),
    CONCLUIDA("Concluida");

    private final String descricao;
}
