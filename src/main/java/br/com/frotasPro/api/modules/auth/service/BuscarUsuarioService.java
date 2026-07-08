package br.com.frotasPro.api.modules.auth.service;

import br.com.frotasPro.api.modules.auth.domain.Usuario;
import br.com.frotasPro.api.modules.auth.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class BuscarUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public Usuario buscarUsuarioPorlogin(String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não encontrado"));

        if (!usuario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário inativo");
        }

        return usuario;
    }

}
