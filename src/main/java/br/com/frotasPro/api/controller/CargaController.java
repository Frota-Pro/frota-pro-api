package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.CargaRequest;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.service.carga.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/carga")
@AllArgsConstructor
public class CargaController {

    private final ListarCargaService listarCargaService;
    private final BuscarCargaService buscarCargaService;
    private final CriarCargaService criarCargaService;
    private final AtualizarCargaService atualizarCargaService;
    private final DeletarCargaService deletarCargaService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/{numeroCarga}")
    public ResponseEntity<CargaResponse> buscarPorNumero(@PathVariable String numeroCarga) {
        CargaResponse carga = buscarCargaService.buscar(numeroCarga);
        return ResponseEntity.ok(carga);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<Page<CargaResponse>> listar(Pageable pageable) {
        Page<CargaResponse> cargas = listarCargaService.listar(pageable);
        return ResponseEntity.ok(cargas);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping
    public ResponseEntity<CargaResponse> registrar(@Valid @RequestBody CargaRequest request) {

        CargaResponse carga = criarCargaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numeroCarga}")
                .buildAndExpand(carga.getNumeroCarga())
                .toUri();

        return ResponseEntity.created(location).body(carga);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/{numeroCarga}")
    public ResponseEntity<CargaResponse> atualizar(@PathVariable String numeroCarga,
                                                   @Valid @RequestBody CargaRequest request) {

        CargaResponse cargaAtualizada = atualizarCargaService.atualizar(numeroCarga, request);
        return ResponseEntity.ok(cargaAtualizada);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @DeleteMapping("/{numeroCarga}")
    public ResponseEntity<Void> deletar(@PathVariable String numeroCarga) {
        deletarCargaService.deletar(numeroCarga);
        return ResponseEntity.noContent().build();
    }
}
