package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@AllArgsConstructor
public class UsuarioAutenticadoService {

    private UsuarioRepository usuarioRepository;

    public String getLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getCredentials();
        return jwt.getClaim("login");
    }
    public Usuario getUsuario() {
        return usuarioRepository.findByLogin(getLogin())
                .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "Usuário não existe ou não está autenticado"));
    }
}
