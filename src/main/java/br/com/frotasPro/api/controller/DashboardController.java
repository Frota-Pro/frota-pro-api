package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.DashboardResumoResponse;
import br.com.frotasPro.api.service.dashboard.BuscarDashboardResumoService;
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

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoResponse> resumo() {
        return ResponseEntity.ok(buscarDashboardResumoService.executar());
    }
}
