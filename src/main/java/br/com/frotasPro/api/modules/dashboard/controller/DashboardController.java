package br.com.frotasPro.api.modules.dashboard.controller;

import br.com.frotasPro.api.modules.dashboard.dto.response.DashboardResumoResponse;
import br.com.frotasPro.api.modules.dashboard.dto.response.DashboardMetasResponse;
import br.com.frotasPro.api.modules.dashboard.service.BuscarDashboardMetasService;
import br.com.frotasPro.api.modules.dashboard.service.BuscarDashboardResumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final BuscarDashboardResumoService buscarDashboardResumoService;
    private final BuscarDashboardMetasService buscarDashboardMetasService;

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoResponse> resumo() {
        return ResponseEntity.ok(buscarDashboardResumoService.executar());
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/metas")
    public ResponseEntity<DashboardMetasResponse> metas() {
        return ResponseEntity.ok(buscarDashboardMetasService.executar());
    }
}
