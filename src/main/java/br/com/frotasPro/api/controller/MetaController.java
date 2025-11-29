package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MetaRequest;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.service.meta.AtualizarMetaService;
import br.com.frotasPro.api.service.meta.BuscarMetaPorIdService;
import br.com.frotasPro.api.service.meta.BuscarTodasMetasService;
import br.com.frotasPro.api.service.meta.CriarMetaService;
import br.com.frotasPro.api.service.meta.DeletarMetaService;
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
@RequestMapping("/metas")
@RequiredArgsConstructor
public class MetaController {

    private final CriarMetaService criarMetaService;
    private final AtualizarMetaService atualizarMetaService;
    private final BuscarMetaPorIdService buscarMetaPorIdService;
    private final BuscarTodasMetasService buscarTodasMetasService;
    private final DeletarMetaService deletarMetaService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    public ResponseEntity<MetaResponse> criar(@Valid @RequestBody MetaRequest request) {

        MetaResponse response = criarMetaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    public ResponseEntity<MetaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody MetaRequest request) {

        MetaResponse response = atualizarMetaService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{id}")
    public ResponseEntity<MetaResponse> buscarPorId(@PathVariable UUID id) {
        MetaResponse response = buscarMetaPorIdService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<MetaResponse>> listar(Pageable pageable) {
        Page<MetaResponse> page = buscarTodasMetasService.listar(pageable);
        return ResponseEntity.ok(page);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        deletarMetaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
