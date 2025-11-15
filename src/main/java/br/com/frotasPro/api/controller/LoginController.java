package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.LoginRequest;
import br.com.frotasPro.api.controller.response.LoginResponse;
import br.com.frotasPro.api.service.usuario.UsuarioLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final UsuarioLoginService usuarioLoginService;

    @PostMapping
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return usuarioLoginService.login(request);
    }
}
