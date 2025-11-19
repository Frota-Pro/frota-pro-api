package br.com.frotasPro.api.controller.integracao;

import br.com.frotasPro.api.service.integracao.IntegracaoCargaService;
import br.com.frotasPro.api.service.integracao.SincronizarCaminhaoIntegracaoService;
import br.com.frotasPro.api.service.integracao.SincronizarMotoristaIntegracaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integracao")
@RequiredArgsConstructor
public class IntegracaoWinThorController {

    private final SincronizarMotoristaIntegracaoService motoristaIntegracaoService;
    private final SincronizarCaminhaoIntegracaoService caminhaoIntegracaoService;
    private final IntegracaoCargaService integracaoCargaService;

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping("/motoristas/sincronizar")
    public ResponseEntity<Void> sincronizarMotoristas(@RequestParam UUID empresaId) {
        motoristaIntegracaoService.dispararSincronizacao(empresaId);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping("/caminhoes/sincronizar")
    public ResponseEntity<Void> sincronizarCaminhoes(@RequestParam UUID empresaId) {
        caminhaoIntegracaoService.sincronizar(empresaId);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/cargas/sincronizar")
    public ResponseEntity<?> sincronizar(
            @RequestParam("empresaId") UUID empresaId,
            @RequestParam("data")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        UUID jobId = integracaoCargaService.solicitarSincronizacao(empresaId, data);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }
}
