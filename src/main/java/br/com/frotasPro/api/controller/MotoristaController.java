package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MotoristaRequest;
import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.service.MotoristaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/motorista")
@AllArgsConstructor
public class MotoristaController {

    private final MotoristaService motoristaService;

    @GetMapping("/{codigo}")
    public ResponseEntity<MotoristaResponse> buscarPorCodigo(@PathVariable String codigo) {
        MotoristaResponse motorista = motoristaService.motoristaPorCodigo(codigo);
        return ResponseEntity.ok(motorista);
    }

    @GetMapping
    public ResponseEntity<Page<MotoristaResponse>> listar(Pageable pageable) {
        Page<MotoristaResponse> motoristas = motoristaService.todosMotoristas(pageable);
        return ResponseEntity.ok(motoristas);
    }

    @PostMapping
    public ResponseEntity<MotoristaResponse> registar(@Valid @RequestBody MotoristaRequest request){

        MotoristaResponse motorista = motoristaService.criarMotorista(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(motorista.getCodigo())
                .toUri();
        return ResponseEntity.created(location).body(motorista);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<MotoristaResponse> atualizar(@PathVariable String codigo, @Valid @RequestBody MotoristaRequest request) {

        MotoristaResponse motoristaAtualizado =
                motoristaService.atualizarMotorista(codigo, request);

        return ResponseEntity.ok(motoristaAtualizado);
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        motoristaService.deletarMotorista(codigo);
        return ResponseEntity.noContent().build();
    }
}
