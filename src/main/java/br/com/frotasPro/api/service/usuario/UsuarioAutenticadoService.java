package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

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

    public UUID getUsuarioIdLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {

            String id = jwt.getClaimAsString("id");

            if (id == null) {
                throw new IllegalStateException("Token JWT não contém o claim 'id'");
            }

            return UUID.fromString(id);
        }

        throw new IllegalStateException(
                "Tipo de principal não suportado: " + principal.getClass().getName());
    }
}
