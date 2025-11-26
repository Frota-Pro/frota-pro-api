package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.EixoRequest;
import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.service.eixo.*;
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
@RequestMapping("/eixo")
@RequiredArgsConstructor
public class EixoController {

    private final CriarEixoService criarEixoService;
    private final BuscarEixoPorIdService buscarEixoPorIdService;
    private final ListarEixosService listarEixosService;
    private final ListarEixosPorCaminhaoService listarEixosPorCaminhaoService;
    private final AtualizarEixoService atualizarEixoService;
    private final DeletarEixoService deletarEixoService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    public ResponseEntity<EixoResponse> criar(@Valid @RequestBody EixoRequest request) {
        EixoResponse eixo = criarEixoService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(eixo.getId())
                .toUri();

        return ResponseEntity.created(location).body(eixo);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/{id}")
    public ResponseEntity<EixoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(buscarEixoPorIdService.buscar(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<EixoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(listarEixosService.listar(pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}")
    public ResponseEntity<Page<EixoResponse>> listarPorCaminhao(
            @PathVariable String codigoCaminhao,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                listarEixosPorCaminhaoService.listarPorCaminhao(codigoCaminhao, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    public ResponseEntity<EixoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EixoRequest request
    ) {
        return ResponseEntity.ok(atualizarEixoService.atualizar(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        deletarEixoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
