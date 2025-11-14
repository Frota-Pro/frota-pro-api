package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.UsuarioRequest;
import br.com.frotasPro.api.controller.response.UsuarioResponse;
import br.com.frotasPro.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public UsuarioResponse registar(@Valid @RequestBody UsuarioRequest request){
        return usuarioService.registar(request);
    }
}
