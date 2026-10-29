package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import org.springframework.stereotype.Component;

/**
 * Contas antigas ainda podem ter a senha fixa "padrao123". Com o parâmetro "bloquear senha padrão"
 * ligado (tela Parâmetros do Sistema) o login com ela é recusado e a pessoa precisa pedir uma senha
 * nova ao administrador. Desligado por padrão: ligar antes de redefinir essas senhas as tranca.
 */
@Component
public class SenhaPadraoPolitica {

    public static final String SENHA_PADRAO = "padrao123";

    private final ParametroSistemaService parametros;

    public SenhaPadraoPolitica(ParametroSistemaService parametros) {
        this.parametros = parametros;
    }

    public boolean deveBloquear(String senhaDigitada) {
        return SENHA_PADRAO.equals(senhaDigitada) && parametros.buscarOuPadrao().isBloquearSenhaPadrao();
    }
}
