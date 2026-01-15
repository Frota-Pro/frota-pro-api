package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.AuthMeResponse;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(Authentication authentication) {
        String login = authentication.getName();

        Usuario usuario = usuarioRepository.findByNome(login)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        AuthMeResponse resp = AuthMeResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .login(usuario.getLogin())
                .authorities(authorities)
                .build();

        return ResponseEntity.ok(resp);
    }
}
