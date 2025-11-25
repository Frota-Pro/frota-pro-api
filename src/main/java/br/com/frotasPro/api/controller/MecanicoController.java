package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MecanicoRequest;
import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.service.mecanico.*;
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
@RequestMapping("/mecanicos")
@RequiredArgsConstructor
public class MecanicoController {

    private final BuscarTodosMecanicosService buscarTodosMecanicosService;
    private final BuscarMecanicosPorOficinaService buscarMecanicosPorOficinaService;
    private final BuscarMecanicosPorCodigoService buscarMecanicosPorCodigoService;
    private final CriarMecanicoService criarMecanicoService;
    private final AtualizarMecanicoService atualizarMecanicoService;
    private final DeletarMecanicoService deletarMecanicoService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<MecanicoResponse>> listar(Pageable pageable) {
        Page<MecanicoResponse> page = buscarTodosMecanicosService.listar(pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN')")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<MecanicoResponse> buscarPorCodigo(@PathVariable String codigo) {
        MecanicoResponse response = buscarMecanicosPorCodigoService.BuscarPorCodigo(codigo);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN')")
    @GetMapping("/oficina/{codigoOficina}")
    public ResponseEntity<Page<MecanicoResponse>> listarPorOficina(
            @PathVariable String codigoOficina,
            Pageable pageable) {

        Page<MecanicoResponse> page = buscarMecanicosPorOficinaService.listarPorOficina(codigoOficina, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<MecanicoResponse> criar(@Valid @RequestBody MecanicoRequest request) {
        MecanicoResponse response = criarMecanicoService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PutMapping("/{codigo}")
    public ResponseEntity<MecanicoResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody MecanicoRequest request) {

        MecanicoResponse response = atualizarMecanicoService.atualizar(codigo, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarMecanicoService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }
}
