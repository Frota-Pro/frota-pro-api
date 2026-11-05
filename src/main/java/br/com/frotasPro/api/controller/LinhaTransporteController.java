package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.LinhaTransporteRequest;
import br.com.frotasPro.api.controller.response.LinhaTransporteResponse;
import br.com.frotasPro.api.service.linhatransporte.LinhaTransporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Cadastro das linhas de transporte (Frios, Linha seca...) que o gerente vincula aos caminhões. */
@RestController
@RequestMapping("/linhas-transporte")
@RequiredArgsConstructor
public class LinhaTransporteController {

    private final LinhaTransporteService service;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping
    public ResponseEntity<List<LinhaTransporteResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    public ResponseEntity<LinhaTransporteResponse> criar(@Valid @RequestBody LinhaTransporteRequest request) {
        LinhaTransporteResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    public ResponseEntity<LinhaTransporteResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody LinhaTransporteRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
