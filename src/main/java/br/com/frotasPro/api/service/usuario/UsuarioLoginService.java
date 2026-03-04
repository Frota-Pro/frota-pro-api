package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.controller.request.LoginRequest;
import br.com.frotasPro.api.controller.response.LoginResponse;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.service.auth.AuthTokenService;
import br.com.frotasPro.api.service.auth.TokenPair;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class UsuarioLoginService {

    private final BuscarUsuarioService buscarUsuarioService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public LoginResponse login( LoginRequest request) {
        Usuario usuario = buscarUsuarioService.buscarUsuarioPorlogin(request.getLogin());

        senhaValidator(request.getSenha(), usuario.getSenha());

        TokenPair tokenPair = authTokenService.generateTokenPair(usuario);

        return new LoginResponse(
                tokenPair.accessToken(),
                tokenPair.accessExpiresIn(),
                tokenPair.refreshToken(),
                tokenPair.refreshExpiresIn()
        );
    }

    private void senhaValidator(String password, String savedPassword) {
        if(!passwordEncoder.matches(password, savedPassword)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
    }
}
