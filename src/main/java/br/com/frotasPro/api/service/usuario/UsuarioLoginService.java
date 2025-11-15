package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.controller.request.LoginRequest;
import br.com.frotasPro.api.controller.response.LoginResponse;
import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.Usuario;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class UsuarioLoginService {

    private final BuscarUsuarioService buscarUsuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public LoginResponse login( LoginRequest request) {
        Usuario usuario = buscarUsuarioService.buscarUsuarioPorlogin(request.getLogin());

        senhaValidator(request.getSenha(), usuario.getSenha());

        List<String> acessos= usuario.getAcesso().stream().map(Acesso::getNome)
                .toList();
        long expiresIn = 60L * 60L * 10L;
        JwtClaimsSet jwt = JwtClaimsSet.builder()
                .issuer("frotaPro-api")
                .subject(usuario.getNome())
                .expiresAt(Instant.now().plusSeconds(expiresIn))
                .issuedAt(Instant.now())
                .claim("login", usuario.getLogin())
                .claim("id", usuario.getId())
                .claim("scope", acessos)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(jwt)).getTokenValue();

        return new LoginResponse(token, expiresIn);
    }

    private void senhaValidator(String password, String savedPassword) {
        if(!passwordEncoder.matches(password, savedPassword)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
    }
}
