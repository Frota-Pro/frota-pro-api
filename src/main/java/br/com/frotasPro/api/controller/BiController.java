package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.BiDashboardResponse;
import br.com.frotasPro.api.controller.response.BiEmbedUrlResponse;
import br.com.frotasPro.api.controller.response.BiOperacaoResponse;
import br.com.frotasPro.api.controller.response.BiPainelResponse;
import br.com.frotasPro.api.service.bi.BiOperacaoService;
import br.com.frotasPro.api.service.bi.BiPainelService;
import br.com.frotasPro.api.service.bi.BiPeriodo;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import br.com.frotasPro.api.service.bi.MetabaseEmbedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** BI (dashboards do Metabase) para o gerente logístico, no app. */
@RestController
@RequestMapping("/bi")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
public class BiController {

    private final MetabaseEmbedService metabaseEmbedService;
    private final BiOperacaoService biOperacaoService;
    private final BiPainelService biPainelService;

    @GetMapping("/dashboards")
    public ResponseEntity<List<BiDashboardResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(metabaseEmbedService.listar(ehAdmin(authentication)));
    }

    /** Painel "Operação" já calculado (KPIs e gráficos), para o app desenhar sem abrir o Metabase. */
    @GetMapping("/operacao")
    public ResponseEntity<BiOperacaoResponse> operacao(
            @RequestParam(required = false) Integer meses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(biOperacaoService.buscar(meses, de, ate));
    }

    /** Dados de todos os cartões de um dashboard, para o app desenhar nativamente. */
    @GetMapping("/dashboards/{id}/dados")
    public ResponseEntity<BiPainelResponse> dados(
            @PathVariable int id,
            @RequestParam(required = false) Integer meses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            Authentication authentication) {
        var periodo = BiPeriodo.resolver(meses, de, ate, FusoHorarioUtils.hojeBrasil());
        return ResponseEntity.ok(biPainelService.buscar(id, ehAdmin(authentication), periodo));
    }

    @GetMapping("/dashboards/{id}/embed")
    public ResponseEntity<BiEmbedUrlResponse> embed(@PathVariable int id, Authentication authentication) {
        return ResponseEntity.ok(metabaseEmbedService.gerarUrl(id, ehAdmin(authentication)));
    }

    private static boolean ehAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
