package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.ClienteResponse;
import br.com.frotasPro.api.service.cliente.ListarClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Consulta do cadastro de clientes (CNPJ/CPF + endereço) — alimentado
 * automaticamente pela leitura de XML de NFe (upload manual ou, do lado da
 * integração WinThor, quando alguém abre o XML de uma nota). Não tem
 * criação/edição manual por aqui: o dado vem sempre da nota fiscal.
 */
@RestController
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ListarClienteService listarClienteService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<Page<ClienteResponse>> listar(
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable
    ) {
        return ResponseEntity.ok(listarClienteService.listar(q, pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(listarClienteService.buscarPorId(id));
    }
}
