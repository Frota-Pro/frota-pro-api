package br.com.frotasPro.api.service.usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Contas antigas ainda podem ter a senha fixa "padrao123". Ligando BLOQUEAR_SENHA_PADRAO=true o
 * login com ela é recusado e a pessoa precisa pedir uma senha nova ao administrador. Fica
 * desligado por padrão para o deploy não trancar ninguém (inclusive o admin) antes de as senhas
 * serem redefinidas.
 */
@Component
public class SenhaPadraoPolitica {

    public static final String SENHA_PADRAO = "padrao123";

    private final boolean bloquear;

    public SenhaPadraoPolitica(@Value("${frotapro.security.bloquear-senha-padrao:false}") boolean bloquear) {
        this.bloquear = bloquear;
    }

    public boolean deveBloquear(String senhaDigitada) {
        return bloquear && SENHA_PADRAO.equals(senhaDigitada);
    }
}
