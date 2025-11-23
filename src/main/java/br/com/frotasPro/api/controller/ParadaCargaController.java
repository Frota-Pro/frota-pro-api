package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.service.paradaCarga.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parada-carga")
public class ParadaCargaController {

    private final CriarParadaCargaService criarService;
    private final BuscarParadaCargaPorIdService buscarPorIdService;
    private final ListarParadaCargaService buscarTodasService;
    private final AtualizarParadaCargaService atualizarService;
    private final DeletarParadaCargaService deletarService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PostMapping
    public ResponseEntity<ParadaCargaResponse> registrar(
            @Valid @RequestBody ParadaCargaRequest request) {

        ParadaCargaResponse parada = criarService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(parada.getId())
                .toUri();

        return ResponseEntity.created(location).body(parada);
    }

    @GetMapping("/{id}")
    public ParadaCargaResponse buscarPorId(@PathVariable UUID id) {
        return buscarPorIdService.buscar(id);
    }

    @GetMapping
    public List<ParadaCargaResponse> listar() {
        return buscarTodasService.listar();
    }

    @PutMapping("/{id}")
    public ParadaCargaResponse atualizar(@PathVariable UUID id,
                                         @RequestBody ParadaCargaRequest request) {
        return atualizarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
