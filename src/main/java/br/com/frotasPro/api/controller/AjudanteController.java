package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.AjudanteRequest;
import br.com.frotasPro.api.controller.request.CaminhaoRequest;
import br.com.frotasPro.api.controller.response.AjudanteResponse;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.service.ajudante.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/ajudantes")
@RequiredArgsConstructor
public class AjudanteController {

    private final ListarAjudanteService listarAjudanteService;
    private final BuscarAjudanteService buscarAjudanteService;
    private final CriarAjudanteService criarAjudanteService;
    private final AtualizarAjudanteService atualizarAjudanteService;
    private final DeletarAjudanteService deletarAjudanteService;

    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\',)")
    @GetMapping
    public ResponseEntity<Page<AjudanteResponse>> listar(Pageable pageable) {
        Page<AjudanteResponse> ajudantes = listarAjudanteService.listar(pageable);
        return ResponseEntity.ok(ajudantes);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\',)")
    @GetMapping("/{codigo}")
    public ResponseEntity<AjudanteResponse> buscarPorCodigo(@PathVariable String codigo) {
        AjudanteResponse ajudante = buscarAjudanteService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(ajudante);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping
    public ResponseEntity<AjudanteResponse> registrar(
            @Valid @RequestBody AjudanteRequest request) {

        AjudanteResponse ajudante = criarAjudanteService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(ajudante.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(ajudante);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PutMapping("/{codigo}")
    public ResponseEntity<AjudanteResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody AjudanteRequest request) {

        AjudanteResponse ajudanteAtualizado = atualizarAjudanteService.atualizar(codigo, request);
        return ResponseEntity.ok(ajudanteAtualizado);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarAjudanteService.desativar(codigo);
        return ResponseEntity.noContent().build();
    }
}
