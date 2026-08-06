package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.PlanoManutencaoPreventivaRequest;
import br.com.frotasPro.api.controller.response.PlanoManutencaoPreventivaResponse;
import br.com.frotasPro.api.service.manutencao.PlanoManutencaoPreventivaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/planos-manutencao-preventiva")
@RequiredArgsConstructor
public class PlanoManutencaoPreventivaController {

    private final PlanoManutencaoPreventivaService service;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    public ResponseEntity<PlanoManutencaoPreventivaResponse> criar(@Valid @RequestBody PlanoManutencaoPreventivaRequest request) {
        PlanoManutencaoPreventivaResponse plano = service.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(plano.getId())
                .toUri();

        return ResponseEntity.created(location).body(plano);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/{id}")
    public ResponseEntity<PlanoManutencaoPreventivaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<PlanoManutencaoPreventivaResponse>> listar(
            @RequestParam(required = false) String caminhao,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listar(caminhao, ativo, pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}")
    public ResponseEntity<Page<PlanoManutencaoPreventivaResponse>> listarPorCaminhao(
            @PathVariable String codigoCaminhao,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarPorCaminhao(codigoCaminhao, pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    public ResponseEntity<PlanoManutencaoPreventivaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PlanoManutencaoPreventivaRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
