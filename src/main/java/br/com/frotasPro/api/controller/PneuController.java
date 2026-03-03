package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.PneuMovimentacaoRequest;
import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuMovimentacaoResponse;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.controller.response.PneuVidaUtilResponse;
import br.com.frotasPro.api.controller.response.RelatorioVidaUtilPneuResponse;
import br.com.frotasPro.api.service.pneu.PneuService;
import br.com.frotasPro.api.service.relatorios.RelatorioVidaUtilPneuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/pneus")
public class PneuController {

    private final PneuService service;
    private final RelatorioVidaUtilPneuService relatorioVidaUtilPneuService;

    @GetMapping
    public ResponseEntity<Page<PneuResponse>> listar(
            @RequestParam(value = "q", required = false)
            @Size(max = 120, message = "Filtro inválido")
            String q,
            @RequestParam(value = "status", required = false)
            @Pattern(
                    regexp = "ESTOQUE|EM_USO|EM_RECAPAGEM|DESCARTADO",
                    message = "Status inválido"
            )
            String status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listar(q, status, pageable));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<PneuResponse> buscar(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 20, message = "Código inválido")
            String codigo
    ) {
        return ResponseEntity.ok(service.buscarPorCodigo(codigo));
    }

    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = "pneu_listar", allEntries = true),
            @CacheEvict(value = "pneu_buscar_codigo", allEntries = true),
            @CacheEvict(value = "pneu_vida_util", allEntries = true),
            @CacheEvict(value = "pneu_movimentacoes", allEntries = true)
    })
    public ResponseEntity<PneuResponse> criar(@Valid @RequestBody PneuRequest req) {
        return ResponseEntity.ok(service.criar(req));
    }

    @PutMapping("/{codigo}")
    @Caching(evict = {
            @CacheEvict(value = "pneu_listar", allEntries = true),
            @CacheEvict(value = "pneu_buscar_codigo", allEntries = true),
            @CacheEvict(value = "pneu_vida_util", allEntries = true),
            @CacheEvict(value = "pneu_movimentacoes", allEntries = true)
    })
    public ResponseEntity<PneuResponse> atualizar(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 20, message = "Código inválido")
            String codigo,
            @Valid @RequestBody PneuRequest req
    ) {
        return ResponseEntity.ok(service.atualizar(codigo, req));
    }

    @DeleteMapping("/{codigo}")
    @Caching(evict = {
            @CacheEvict(value = "pneu_listar", allEntries = true),
            @CacheEvict(value = "pneu_buscar_codigo", allEntries = true),
            @CacheEvict(value = "pneu_vida_util", allEntries = true),
            @CacheEvict(value = "pneu_movimentacoes", allEntries = true)
    })
    public ResponseEntity<Void> deletar(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 20, message = "Código inválido")
            String codigo
    ) {
        service.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    // VIDA ÚTIL
    @GetMapping("/{codigo}/vida-util")
    public ResponseEntity<PneuVidaUtilResponse> vidaUtil(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 20, message = "Código inválido")
            String codigo
    ) {
        return ResponseEntity.ok(service.vidaUtil(codigo));
    }

    @GetMapping("/relatorios/vida-util")
    public ResponseEntity<RelatorioVidaUtilPneuResponse> relatorioVidaUtil(
            @RequestParam(value = "caminhao", required = false)
            @Size(max = 50, message = "Código do caminhão inválido")
            String codigoCaminhao,
            @RequestParam(value = "pneu", required = false)
            @Size(max = 20, message = "Código do pneu inválido")
            String codigoPneu
    ) {
        return ResponseEntity.ok(relatorioVidaUtilPneuService.gerar(codigoCaminhao, codigoPneu));
    }

    // EVENTOS
    @PostMapping("/{codigo}/movimentacoes")
    @Caching(evict = {
            @CacheEvict(value = "pneu_listar", allEntries = true),
            @CacheEvict(value = "pneu_buscar_codigo", allEntries = true),
            @CacheEvict(value = "pneu_vida_util", allEntries = true),
            @CacheEvict(value = "pneu_movimentacoes", allEntries = true)
    })
    public ResponseEntity<Void> movimentacao(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 20, message = "Código inválido")
            String codigo,
            @Valid @RequestBody PneuMovimentacaoRequest req
    ) {
        service.registrarMovimentacao(codigo, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codigo}/movimentacoes")
    public ResponseEntity<Page<PneuMovimentacaoResponse>> listarMovimentacoes(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 20, message = "Código inválido")
            String codigo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarMovimentacoes(codigo, pageable));
    }
}
