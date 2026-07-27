package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.DashboardResumoResponse;
import br.com.frotasPro.api.controller.response.DashboardMetasResponse;
import br.com.frotasPro.api.controller.response.SaudeSistemaResponse;
import br.com.frotasPro.api.service.dashboard.BuscarDashboardMetasService;
import br.com.frotasPro.api.service.dashboard.BuscarDashboardResumoService;
import br.com.frotasPro.api.service.dashboard.BuscarSaudeSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final BuscarDashboardResumoService buscarDashboardResumoService;
    private final BuscarDashboardMetasService buscarDashboardMetasService;
    private final BuscarSaudeSistemaService buscarSaudeSistemaService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoResponse> resumo() {
        return ResponseEntity.ok(buscarDashboardResumoService.executar());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/metas")
    public ResponseEntity<DashboardMetasResponse> metas() {
        return ResponseEntity.ok(buscarDashboardMetasService.executar());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/saude-sistema")
    public ResponseEntity<SaudeSistemaResponse> saudeSistema(
            @RequestParam(value = "inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(buscarSaudeSistemaService.buscar(inicio, fim));
    }
}
