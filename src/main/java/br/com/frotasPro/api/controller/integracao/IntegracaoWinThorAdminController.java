package br.com.frotasPro.api.controller.integracao;

import br.com.frotasPro.api.controller.integracao.dto.*;
import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.service.integracao.*;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorMonitorService.TipoJob;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/integracao/winthor")
@RequiredArgsConstructor
public class IntegracaoWinThorAdminController {

    private final IntegracaoWinThorConfigService configService;
    private final IntegracaoWinThorMonitorService monitorService;
    private final IntegracaoWinThorStatusService statusService;

    private final IntegracaoMotoristaService motoristaIntegracaoService;
    private final IntegracaoCaminhaoService caminhaoIntegracaoService;
    private final IntegracaoCargaService cargaIntegracaoService;

    @org.springframework.beans.factory.annotation.Value("${frotapro.empresa-sync-id}")
    private UUID empresaIdPadrao;

    private UUID empresa(UUID empresaId) {
        return empresaId != null ? empresaId : empresaIdPadrao;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/config")
    public ResponseEntity<IntegracaoWinThorConfigResponse> buscarConfig(
            @RequestParam(value = "empresaId", required = false) UUID empresaId
    ) {
        return ResponseEntity.ok(configService.buscar(empresa(empresaId)));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/config")
    public ResponseEntity<IntegracaoWinThorConfigResponse> atualizarConfig(
            @RequestParam(value = "empresaId", required = false) UUID empresaId,
            @Valid @RequestBody IntegracaoWinThorConfigUpdateRequest req
    ) {
        return ResponseEntity.ok(configService.atualizar(empresa(empresaId), req));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/jobs")
    public ResponseEntity<List<IntegracaoWinThorJobResponse>> listarJobs(
            @RequestParam(value = "empresaId", required = false) UUID empresaId,
            @RequestParam(value = "tipo", defaultValue = "TODOS") String tipo,
            @RequestParam(value = "status") String statusCsv,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        TipoJob t = TipoJob.valueOf(tipo.trim().toUpperCase());
        Collection<StatusSincronizacao> st = parseStatus(statusCsv);
        return ResponseEntity.ok(monitorService.listarJobs(empresa(empresaId), t, st, page, size));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/jobs/{tipo}/{jobId}/retry")
    public ResponseEntity<?> retry(
            @RequestParam(value = "empresaId", required = false) UUID empresaId,
            @PathVariable("tipo") String tipo,
            @PathVariable("jobId") UUID jobId
    ) {
        TipoJob t = TipoJob.valueOf(tipo.trim().toUpperCase());
        monitorService.retry(empresa(empresaId), t, jobId);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId, "tipo", t.name()));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/status")
    public ResponseEntity<IntegracaoWinThorStatusResponse> status() {
        return ResponseEntity.ok(statusService.verificar());
    }

    // ============================
    // Disparo manual (empresaId fixo por enquanto)
    // ============================

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/sync/motoristas")
    public ResponseEntity<?> syncMotoristas() {
        UUID jobId = motoristaIntegracaoService.solicitarSincronizacao(empresaIdPadrao);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/sync/caminhoes")
    public ResponseEntity<?> syncCaminhoes(@RequestParam(value = "codFilial", required = false) Integer codFilial) {
        UUID jobId = caminhaoIntegracaoService.solicitarSincronizacao(empresaIdPadrao, codFilial);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/sync/cargas")
    public ResponseEntity<?> syncCargas(
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        if (data == null) data = LocalDate.now();
        UUID jobId = cargaIntegracaoService.solicitarSincronizacao(empresaIdPadrao, data);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    private Collection<StatusSincronizacao> parseStatus(String csv) {
        if (csv == null || csv.isBlank()) return List.of(StatusSincronizacao.values());
        String[] parts = csv.split(",");
        List<StatusSincronizacao> out = new ArrayList<>();
        for (String p : parts) {
            String s = p.trim();
            if (!s.isEmpty()) out.add(StatusSincronizacao.valueOf(s.toUpperCase()));
        }
        return out;
    }
}
