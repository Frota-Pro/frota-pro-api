package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.PneuMovimentacaoRequest;
import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.controller.response.PneuVidaUtilResponse;
import br.com.frotasPro.api.service.pneu.PneuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pneus")
public class PneuController {

    private final PneuService service;

    @GetMapping
    public ResponseEntity<Page<PneuResponse>> listar(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listar(q, status, pageable));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<PneuResponse> buscar(@PathVariable String codigo) {
        return ResponseEntity.ok(service.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<PneuResponse> criar(@Valid @RequestBody PneuRequest req) {
        return ResponseEntity.ok(service.criar(req));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<PneuResponse> atualizar(@PathVariable String codigo, @Valid @RequestBody PneuRequest req) {
        return ResponseEntity.ok(service.atualizar(codigo, req));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        service.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    // VIDA ÚTIL
    @GetMapping("/{codigo}/vida-util")
    public ResponseEntity<PneuVidaUtilResponse> vidaUtil(@PathVariable String codigo) {
        return ResponseEntity.ok(service.vidaUtil(codigo));
    }

    // EVENTOS
    @PostMapping("/{codigo}/movimentacoes")
    public ResponseEntity<Void> movimentacao(@PathVariable String codigo,@Valid @RequestBody PneuMovimentacaoRequest req) {
        service.registrarMovimentacao(codigo, req);
        return ResponseEntity.ok().build();
    }
}
