package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ContaRequest;
import br.com.frotasPro.api.controller.response.ContaResponse;
import br.com.frotasPro.api.domain.Conta;
import br.com.frotasPro.api.service.conta.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conta")
public class ContaController {

    private final CriarContaService criarService;
    private final BuscarContaPorIdService buscarPorIdService;
    private final BuscarContaPorCodigo buscarContaPorCodigo;
    private final ListarContaService listarService;
    private final AtualizarContaService atualizarService;
    private final DeletarContaService deletarService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContaResponse criar(@Valid @RequestBody ContaRequest request) {
        return criarService.criar(request);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable UUID id) {
        ContaResponse conta = buscarPorIdService.buscarPorId(id);
        return ResponseEntity.ok(conta);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping
    public List<ContaResponse> listar() {
        return listarService.listar();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/codigo")
    public ResponseEntity<ContaResponse> buscarContaPorCodigo(@RequestParam("codigo") String codigo){
        ContaResponse conta = buscarContaPorCodigo.buscarPorcodigo(codigo);
        return ResponseEntity.ok(conta);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    public ContaResponse atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ContaRequest request
    ) {
        return atualizarService.atualizar(id, request);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
