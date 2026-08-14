package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.PostoAbastecimentoRequest;
import br.com.frotasPro.api.controller.response.PostoAbastecimentoResponse;
import br.com.frotasPro.api.service.postoAbastecimento.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/postos-abastecimento")
@RequiredArgsConstructor
public class PostoAbastecimentoController {

    private final CriarPostoAbastecimentoService criarService;
    private final AtualizarPostoAbastecimentoService atualizarService;
    private final BuscarPostoAbastecimentoService buscarService;
    private final ListarPostoAbastecimentoService listarService;
    private final DeletarPostoAbastecimentoService deletarService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    public ResponseEntity<PostoAbastecimentoResponse> criar(
            @Valid @RequestBody PostoAbastecimentoRequest request) {

        PostoAbastecimentoResponse response = criarService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(response.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    public ResponseEntity<PostoAbastecimentoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PostoAbastecimentoRequest request) {

        PostoAbastecimentoResponse response = atualizarService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{codigo}")
    public ResponseEntity<PostoAbastecimentoResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(buscarService.buscarPorCodigo(codigo));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<PostoAbastecimentoResponse>> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(listarService.listar(ativo, q, pageable));
    }

    /**
     * Lista enxuta (sem paginação) dos postos ativos — usada pelo seletor no app do
     * motorista, que precisa da lista inteira pra oferecer busca offline-first.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @GetMapping("/ativos")
    public ResponseEntity<List<PostoAbastecimentoResponse>> listarAtivos() {
        return ResponseEntity.ok(listarService.listarAtivos());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }
}
