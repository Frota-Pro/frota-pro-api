package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AcaoAuditoria {
    LOGIN_SUCESSO("Login"),
    LOGIN_FALHA("Tentativa de login"),
    LOGOUT("Logout"),
    CRIACAO("Criação"),
    ATUALIZACAO("Atualização"),
    EXCLUSAO("Exclusão");

    private final String label;
}
