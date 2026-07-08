package br.com.frotasPro.api.modules.auth.mapper;

import br.com.frotasPro.api.modules.auth.dto.response.UsuarioResponse;
import br.com.frotasPro.api.modules.auth.domain.Usuario;

public class UsuarioMapper {
    public static UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .login(usuario.getLogin())
                .nome(usuario.getNome())
                .ativo(usuario.isAtivo())
                .acessos(usuario.getAcesso().stream().map(a -> a.getNome()).toList())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .build();
    }
}
