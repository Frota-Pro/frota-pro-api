package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ClienteRequest;
import br.com.frotasPro.api.controller.response.ClienteResponse;
import br.com.frotasPro.api.service.cliente.AtualizarClienteService;
import br.com.frotasPro.api.service.cliente.CriarClienteService;
import br.com.frotasPro.api.service.cliente.ListarClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Cadastro de clientes (CNPJ/CPF + endereço). Alimentado automaticamente
 * pela leitura de XML de NFe (upload manual ou, do lado da integração
 * WinThor, quando alguém abre o XML de uma nota) — mas também dá pra
 * cadastrar/editar na mão, sem depender de nenhuma nota chegar antes.
 */
@RestController
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ListarClienteService listarClienteService;
    private final CriarClienteService criarClienteService;
    private final AtualizarClienteService atualizarClienteService;

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

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping
    public ResponseEntity<ClienteResponse> registrar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse cliente = criarClienteService.criar(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cliente.getId())
                .toUri();

        return ResponseEntity.created(uri).body(cliente);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteRequest request
    ) {
        return ResponseEntity.ok(atualizarClienteService.atualizar(id, request));
    }
}
