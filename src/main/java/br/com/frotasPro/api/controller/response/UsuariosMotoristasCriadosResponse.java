package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Resultado de criar usuários a partir de motoristas. As senhas iniciais só aparecem aqui,
 * uma vez: ficam gravadas apenas com hash, então quem criou precisa anotá-las na hora.
 */
@Getter
@Builder
public class UsuariosMotoristasCriadosResponse {

    private List<String> mensagens;
    private List<Credencial> credenciais;

    @Getter
    @Builder
    public static class Credencial {
        private String nome;
        private String login;
        private String senhaInicial;
    }
}
