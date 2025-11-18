package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.service.IntegracaoCargaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integracao/cargas")
@RequiredArgsConstructor
public class IntegracaoCargaController {

    private final IntegracaoCargaService integracaoCargaService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/sincronizar")
    public ResponseEntity<?> sincronizar(
            @RequestParam("empresaId") UUID empresaId,
            @RequestParam("data")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        UUID jobId = integracaoCargaService.solicitarSincronizacao(empresaId, data);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }
}
