package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.AnalyticsAbastecimentoResponse;
import br.com.frotasPro.api.controller.response.AnalyticsCaminhaoResponse;
import br.com.frotasPro.api.controller.response.AnalyticsFrotaResponse;
import br.com.frotasPro.api.controller.response.AnalyticsMotoristaResponse;
import br.com.frotasPro.api.service.analytics.AnalyticsAbastecimentoService;
import br.com.frotasPro.api.service.analytics.AnalyticsCaminhaoService;
import br.com.frotasPro.api.service.analytics.AnalyticsFrotaService;
import br.com.frotasPro.api.service.analytics.AnalyticsMotoristaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private static final String ROLES_LEITURA = "hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_CONSULTA')";

    private final AnalyticsFrotaService analyticsFrotaService;
    private final AnalyticsMotoristaService analyticsMotoristaService;
    private final AnalyticsCaminhaoService analyticsCaminhaoService;
    private final AnalyticsAbastecimentoService analyticsAbastecimentoService;

    @PreAuthorize(ROLES_LEITURA)
    @GetMapping("/frota")
    public ResponseEntity<AnalyticsFrotaResponse> frota(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(analyticsFrotaService.gerar(inicio, fim));
    }

    @PreAuthorize(ROLES_LEITURA)
    @GetMapping("/motorista/{codigo}")
    public ResponseEntity<AnalyticsMotoristaResponse> motorista(
            @PathVariable String codigo,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(analyticsMotoristaService.gerar(codigo, inicio, fim));
    }

    @PreAuthorize(ROLES_LEITURA)
    @GetMapping("/caminhao/{codigo}")
    public ResponseEntity<AnalyticsCaminhaoResponse> caminhao(
            @PathVariable String codigo,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(analyticsCaminhaoService.gerar(codigo, inicio, fim));
    }

    @PreAuthorize(ROLES_LEITURA)
    @GetMapping("/abastecimento")
    public ResponseEntity<AnalyticsAbastecimentoResponse> abastecimento(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(analyticsAbastecimentoService.gerar(inicio, fim));
    }
}
