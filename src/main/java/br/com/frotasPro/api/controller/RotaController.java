package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.RotaRequest;
import br.com.frotasPro.api.controller.response.RotaResponse;
import br.com.frotasPro.api.service.rota.*;
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
@RequestMapping("/rota")
@AllArgsConstructor
public class RotaController {

    private final ListarRotaService listarRotaService;
    private final BuscarRotaService buscarRotaService;
    private final CriarRotaService criarRotaService;
    private final AtualizarRotaService atualizarRotaService;
    private final DeletarRotaService deletarRotaService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/{codigo}")
    public ResponseEntity<RotaResponse> buscarPorCodigo(@PathVariable String codigo) {
        RotaResponse rota = buscarRotaService.buscar(codigo);
        return ResponseEntity.ok(rota);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<Page<RotaResponse>> listar(Pageable pageable) {
        Page<RotaResponse> rotas = listarRotaService.listar(pageable);
        return ResponseEntity.ok(rotas);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping
    public ResponseEntity<RotaResponse> registrar(@Valid @RequestBody RotaRequest request) {

        RotaResponse rota = criarRotaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(rota.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(rota);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/{codigo}")
    public ResponseEntity<RotaResponse> atualizar(@PathVariable String codigo,
                                                  @Valid @RequestBody RotaRequest request) {

        RotaResponse rotaAtualizada = atualizarRotaService.atualizar(codigo, request);
        return ResponseEntity.ok(rotaAtualizada);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarRotaService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }
}
