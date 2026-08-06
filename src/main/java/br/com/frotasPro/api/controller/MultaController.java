package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.AtualizarStatusMultaRequest;
import br.com.frotasPro.api.controller.request.MultaRequest;
import br.com.frotasPro.api.controller.response.MultaAnexoResponse;
import br.com.frotasPro.api.controller.response.MultaResponse;
import br.com.frotasPro.api.domain.enums.StatusPagamentoMulta;
import br.com.frotasPro.api.domain.enums.TipoAnexoMulta;
import br.com.frotasPro.api.service.multa.MultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/multas")
@RequiredArgsConstructor
public class MultaController {

    private final MultaService multaService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    public ResponseEntity<MultaResponse> criar(@Valid @RequestBody MultaRequest request) {
        MultaResponse multa = multaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(multa.getId())
                .toUri();

        return ResponseEntity.created(location).body(multa);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/{id}")
    public ResponseEntity<MultaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(multaService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<MultaResponse>> listar(
            @RequestParam(required = false) String caminhao,
            @RequestParam(required = false) String motorista,
            @RequestParam(required = false) StatusPagamentoMulta status,
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(multaService.listar(caminhao, motorista, status, inicio, fim, pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    public ResponseEntity<MultaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody MultaRequest request
    ) {
        return ResponseEntity.ok(multaService.atualizar(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<MultaResponse> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusMultaRequest request
    ) {
        return ResponseEntity.ok(multaService.atualizarStatus(id, request.getStatusPagamento()));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        multaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping(value = "/{id}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultaAnexoResponse> uploadAnexo(
            @PathVariable UUID id,
            @RequestParam("tipoAnexo") TipoAnexoMulta tipoAnexo,
            @RequestPart("arquivo") MultipartFile arquivo
    ) {
        MultaAnexoResponse response = multaService.registrarAnexo(id, tipoAnexo, arquivo);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{anexoId}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{id}/anexos")
    public ResponseEntity<List<MultaAnexoResponse>> listarAnexos(@PathVariable UUID id) {
        return ResponseEntity.ok(multaService.listarAnexos(id));
    }
}
