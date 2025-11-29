package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.CategoriaCaminhaoRequest;
import br.com.frotasPro.api.controller.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.service.categoriaCaminhao.*;
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
@RequestMapping("/categorias-caminhao")
@RequiredArgsConstructor
public class CategoriaCaminhaoController {

    private final CriarCategoriaCaminhaoService criarService;
    private final AtualizarCategoriaCaminhaoService atualizarService;
    private final BuscarCategoriaCaminhaoService buscarService;
    private final ListarCategoriaCaminhaoService listarService;
    private final DeletarCategoriaCaminhaoService deletarService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    public ResponseEntity<CategoriaCaminhaoResponse> criar(
            @Valid @RequestBody CategoriaCaminhaoRequest request) {

        CategoriaCaminhaoResponse response = criarService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaCaminhaoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CategoriaCaminhaoRequest request) {

        CategoriaCaminhaoResponse response = atualizarService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{codigo}")
    public ResponseEntity<CategoriaCaminhaoResponse> buscarPorId(@PathVariable String codigo) {
        CategoriaCaminhaoResponse response = buscarService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<CategoriaCaminhaoResponse>> listar(Pageable pageable) {
        Page<CategoriaCaminhaoResponse> page = listarService.listar(pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }
}
