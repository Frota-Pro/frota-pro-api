package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MotoristaRequest;
import br.com.frotasPro.api.controller.request.UsuarioRequest;
import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.controller.response.UsuarioResponse;
import br.com.frotasPro.api.service.MotoristaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/motorista")
@AllArgsConstructor
public class MotoristaController {

    private final MotoristaService motoristaService;

    @PostMapping
    public ResponseEntity<MotoristaResponse> registar(@Valid @RequestBody MotoristaRequest request){

        MotoristaResponse motorista = motoristaService.criarMotorista(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(motorista.getCodigo())
                .toUri();
        return ResponseEntity.created(location).body(motorista);
    }
}
