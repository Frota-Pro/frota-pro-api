package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.PlanejamentoRequests;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResumoResponse;
import br.com.frotasPro.api.service.planejamento.PlanejamentoCargasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Planejamento de cargas. Ver exige só consulta; criar, atualizar, agrupar, escolher motorista/caminhão, fechar e
 * reabrir exigem Operador de logística ou Admin (o Gerente só consulta).
 */
@RestController
@RequestMapping("/planejamento")
@RequiredArgsConstructor
public class PlanejamentoCargasController {

    private static final String PODE_EDITAR = "hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERADOR_LOGISTICA')";

    private final PlanejamentoCargasService service;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/planilhas")
    public ResponseEntity<List<PlanejamentoPlanilhaResumoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/planilhas/{id}")
    public ResponseEntity<PlanejamentoPlanilhaResponse> obter(@PathVariable UUID id) {
        return ResponseEntity.ok(service.obter(id));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas")
    public ResponseEntity<PlanejamentoPlanilhaResponse> criar(@RequestBody(required = false) PlanejamentoRequests.CriarPlanilha request) {
        return ResponseEntity.ok(service.criar(request != null ? request.getData() : null));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/atualizar-planilha")
    public ResponseEntity<PlanejamentoPlanilhaResponse> atualizar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.atualizar(id));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/fechar-planilha")
    public ResponseEntity<PlanejamentoPlanilhaResponse> fechar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.fechar(id));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/reabrir-planilha")
    public ResponseEntity<PlanejamentoPlanilhaResponse> reabrir(@PathVariable UUID id) {
        return ResponseEntity.ok(service.reabrir(id));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/carregamentos/{numero}/tirar-carregamento")
    public ResponseEntity<PlanejamentoPlanilhaResponse> tirar(@PathVariable UUID id, @PathVariable Integer numero) {
        return ResponseEntity.ok(service.remover(id, numero));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/carregamentos/{numero}/definir-complemento")
    public ResponseEntity<PlanejamentoPlanilhaResponse> definirComplemento(
            @PathVariable UUID id,
            @PathVariable Integer numero,
            @RequestBody PlanejamentoRequests.DefinirComplemento request) {
        return ResponseEntity.ok(service.definirComplemento(id, numero, request.getPrincipal()));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/tirar-carregamentos")
    public ResponseEntity<PlanejamentoPlanilhaResponse> tirarVarios(@PathVariable UUID id, @RequestBody PlanejamentoRequests.Lote request) {
        return ResponseEntity.ok(service.removerVarios(id, request.getNumeros()));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/juntar-carregamentos")
    public ResponseEntity<PlanejamentoPlanilhaResponse> juntar(@PathVariable UUID id, @RequestBody PlanejamentoRequests.Juntar request) {
        return ResponseEntity.ok(service.juntar(id, request.getPrincipal(), request.getNumeros()));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/separar-carregamentos")
    public ResponseEntity<PlanejamentoPlanilhaResponse> separar(@PathVariable UUID id, @RequestBody PlanejamentoRequests.Lote request) {
        return ResponseEntity.ok(service.separar(id, request.getNumeros()));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/carregamentos/{numero}/definir-saida")
    public ResponseEntity<PlanejamentoPlanilhaResponse> definirSaida(
            @PathVariable UUID id,
            @PathVariable Integer numero,
            @RequestBody PlanejamentoRequests.DefinirSaida request) {
        return ResponseEntity.ok(service.definirSaida(id, numero, request.getBoxNumero(), request.getDiaSaida(), request.getTurnoSaida()));
    }

    @PreAuthorize(PODE_EDITAR)
    @PostMapping("/planilhas/{id}/carregamentos/{numero}/definir-rota")
    public ResponseEntity<PlanejamentoPlanilhaResponse> definirRota(
            @PathVariable UUID id,
            @PathVariable Integer numero,
            @RequestBody PlanejamentoRequests.DefinirRota request) {
        return ResponseEntity.ok(service.definirRota(id, numero, request.getMotoristaCodigo(), request.getCaminhaoCodigo(), request.getAjudantesCodigos()));
    }
}
