package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.service.carga.ContarCargasLancamentoManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Cargas que precisam de lançamento manual de km e abastecimento (motorista de terceiro e manifesto sem nota). */
@RestController
@RequestMapping("/cargas/lancamento-manual")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
public class CargaLancamentoManualController {

    private final ContarCargasLancamentoManualService service;

    @GetMapping("/quantidade")
    public ResponseEntity<Map<String, Long>> quantidade() {
        return ResponseEntity.ok(Map.of("quantidade", service.contar()));
    }
}
