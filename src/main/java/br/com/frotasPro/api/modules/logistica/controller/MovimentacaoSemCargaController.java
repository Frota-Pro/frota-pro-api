package br.com.frotasPro.api.modules.logistica.controller;

import br.com.frotasPro.api.modules.logistica.dto.response.MovimentacaoSemCargaResponse;
import br.com.frotasPro.api.modules.logistica.dto.response.ResumoMovimentacaoSemCargaResponse;
import br.com.frotasPro.api.modules.logistica.service.ListarMovimentacaoSemCargaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/movimentacoes-sem-carga")
@RequiredArgsConstructor
public class MovimentacaoSemCargaController {

    private final ListarMovimentacaoSemCargaService service;

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<Page<MovimentacaoSemCargaResponse>> listar(
            @RequestParam(value = "codigoCaminhao", required = false) String codigoCaminhao,
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listar(codigoCaminhao, inicio, fim, pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/resumo")
    public ResponseEntity<ResumoMovimentacaoSemCargaResponse> resumo(
            @RequestParam(value = "codigoCaminhao", required = false) String codigoCaminhao,
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(service.resumo(codigoCaminhao, inicio, fim));
    }
}
