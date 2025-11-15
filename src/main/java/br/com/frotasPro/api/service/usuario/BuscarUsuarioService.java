package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class BuscarUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public Usuario buscarUsuarioPorlogin(String login) {
        return usuarioRepository.findByLogin(login).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não encontrado"));
    }

}
