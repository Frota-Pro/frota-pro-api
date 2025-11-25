package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.OficinaRequest;
import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.service.oficina.*;
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
@RequestMapping("/oficinas")
@RequiredArgsConstructor
public class OficinaController {

    private final BuscarTodasOficinasService buscarTodasOficinasService;
    private final BuscarOficinaPorCodigoService buscarOficinaPorCodigoService;
    private final CriarOficinaService criarOficinaService;
    private final AtualizarOficinaService atualizarOficinaService;
    private final DeletarOficinaService deletarOficinaService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<OficinaResponse>> listar(Pageable pageable) {
        Page<OficinaResponse> page = buscarTodasOficinasService.listar(pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN')")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<OficinaResponse> buscarPorCodigo(@PathVariable String codigo) {
        OficinaResponse response = buscarOficinaPorCodigoService.porCodigo(codigo);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<OficinaResponse> criar(@Valid @RequestBody OficinaRequest request) {
        OficinaResponse response = criarOficinaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PutMapping("/{codigo}")
    public ResponseEntity<OficinaResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody OficinaRequest request) {

        OficinaResponse response = atualizarOficinaService.atualizar(codigo, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarOficinaService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }
}
