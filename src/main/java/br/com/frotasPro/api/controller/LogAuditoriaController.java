package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.LogAuditoriaResponse;
import br.com.frotasPro.api.service.auditoria.ListarLogAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
public class LogAuditoriaController {

    private final ListarLogAuditoriaService listarService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<LogAuditoriaResponse>> listar(
            @RequestParam("dataInicio") LocalDate dataInicio,
            @RequestParam("dataFim") LocalDate dataFim,
            @RequestParam(value = "usuarioLogin", required = false) String usuarioLogin,
            Pageable pageable
    ) {
        return ResponseEntity.ok(listarService.listar(dataInicio, dataFim, usuarioLogin, pageable));
    }
}
