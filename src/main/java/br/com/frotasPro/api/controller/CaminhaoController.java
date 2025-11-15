package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.CaminhaoRequest;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.service.CaminhaoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/caminhao")
@AllArgsConstructor
public class CaminhaoController {

    private final CaminhaoService caminhaoService;


    @GetMapping
    public ResponseEntity<Page<CaminhaoResponse>> listar(Pageable pageable) {
        Page<CaminhaoResponse> caminhoes = caminhaoService.todosCaminhoes(pageable);
        return ResponseEntity.ok(caminhoes);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CaminhaoResponse> buscarPorCodigo(@PathVariable String codigo) {
        CaminhaoResponse caminhao = caminhaoService.caminhaoPorCodigo(codigo);
        return ResponseEntity.ok(caminhao);
    }

    @PostMapping
    public ResponseEntity<CaminhaoResponse> registrar(
            @Valid @RequestBody CaminhaoRequest request) {

        CaminhaoResponse caminhao = caminhaoService.criarCaminhao(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(caminhao.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(caminhao);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<CaminhaoResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody CaminhaoRequest request) {

        CaminhaoResponse caminhaoAtualizado = caminhaoService.atualizarCaminhao(codigo, request);
        return ResponseEntity.ok(caminhaoAtualizado);
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        caminhaoService.deletarCaminhao(codigo);
        return ResponseEntity.noContent().build();
    }
}
