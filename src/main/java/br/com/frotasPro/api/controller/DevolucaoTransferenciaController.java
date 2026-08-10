package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.DevolucaoResponse;
import br.com.frotasPro.api.controller.response.TransferenciaResponse;
import br.com.frotasPro.api.service.notafiscal.DevolucaoTransferenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Detalhe de devolução (produto a produto) e transferência de pedido
 * (nota a nota) de uma carga, buscado ao vivo no WinThor. Nada fica salvo
 * aqui — complementa os resumos já gravados na própria carga.
 */
@RestController
@RequestMapping("/carga/{numeroCarga}")
@RequiredArgsConstructor
public class DevolucaoTransferenciaController {

    private final DevolucaoTransferenciaService service;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_MOTORISTA')")
    @GetMapping("/devolucoes")
    public ResponseEntity<List<DevolucaoResponse>> devolucoes(@PathVariable String numeroCarga) {
        return ResponseEntity.ok(service.buscarDevolucoes(numeroCarga));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_MOTORISTA')")
    @GetMapping("/transferencias")
    public ResponseEntity<List<TransferenciaResponse>> transferencias(@PathVariable String numeroCarga) {
        return ResponseEntity.ok(service.buscarTransferencias(numeroCarga));
    }
}
