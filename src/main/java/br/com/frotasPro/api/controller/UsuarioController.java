package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.UsuarioRequest;
import br.com.frotasPro.api.controller.response.UsuarioResponse;
import br.com.frotasPro.api.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public UsuarioResponse registar(@Valid @RequestBody UsuarioRequest request){
        return usuarioService.registar(request);
    }

    @PostMapping("/motoristas")
    public ResponseEntity<List<String>> criarUsuariosPelosMotoristas(@RequestParam("matriculas") List<String> codigo) {
        List<String> resultado = usuarioService.criarUsuariosPelosMotoristas(codigo);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
