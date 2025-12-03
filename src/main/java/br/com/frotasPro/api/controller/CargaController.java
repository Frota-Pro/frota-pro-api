package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.CargaRequest;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.service.carga.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/carga")
@AllArgsConstructor
public class CargaController {

    private final ListarCargaService listarCargaService;
    private final BuscarCargaService buscarCargaService;
    private final CriarCargaService criarCargaService;
    private final AtualizarCargaService atualizarCargaService;
    private final DeletarCargaService deletarCargaService;
    private final IniciarCargaService iniciarCargaService;
    private final FinalizarCargaService finalizarCargaService;
    private final BuscarCargaAtualMotoristaService buscarCargaAtualMotoristaService;

    // ========= BUSCA ÚNICA =========

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/{numeroCarga}")
    public ResponseEntity<CargaResponse> buscarPorNumero(@PathVariable String numeroCarga) {
        CargaResponse carga = buscarCargaService.porCodigo(numeroCarga);
        return ResponseEntity.ok(carga);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/externo/{codigoExterno}")
    public ResponseEntity<CargaResponse> buscarPorCodigoExterno(@PathVariable String codigoExterno) {
        CargaResponse carga = buscarCargaService.porCodigoExterno(codigoExterno);
        return ResponseEntity.ok(carga);
    }

    // ========= LISTAGEM GERAL =========

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<Page<CargaResponse>> listar(Pageable pageable) {
        Page<CargaResponse> cargas = listarCargaService.listar(pageable);
        return ResponseEntity.ok(cargas);
    }

    // ========= BUSCAS POR DATA =========

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/data-saida")
    public ResponseEntity<Page<CargaResponse>> buscarPorDataSaida(
            @RequestParam("data")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataSaida,
            Pageable pageable
    ) {
        Page<CargaResponse> cargas = buscarCargaService.porDataSaida(dataSaida, pageable);
        return ResponseEntity.ok(cargas);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/periodo-saida")
    public ResponseEntity<Page<CargaResponse>> buscarPorPeriodoSaida(
            @RequestParam("inicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        Page<CargaResponse> cargas = buscarCargaService.porPeriodoSaida(inicio, fim, pageable);
        return ResponseEntity.ok(cargas);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/periodo-criacao")
    public ResponseEntity<Page<CargaResponse>> buscarPorPeriodoCriacao(
            @RequestParam("inicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable
    ) {
        Page<CargaResponse> cargas = buscarCargaService.porPeriodoCriacao(inicio, fim, pageable);
        return ResponseEntity.ok(cargas);
    }

    // ========= BUSCAS POR MOTORISTA / CAMINHÃO =========

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/motorista")
    public ResponseEntity<Page<CargaResponse>> buscarPorMotorista(
            @RequestParam("codigo") String codigoMotorista,
            Pageable pageable
    ) {
        Page<CargaResponse> cargas = buscarCargaService.porMotorista(codigoMotorista, pageable);
        return ResponseEntity.ok(cargas);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/caminhao")
    public ResponseEntity<Page<CargaResponse>> buscarPorCaminhao(
            @RequestParam("codigo") String codigoCaminhao,
            Pageable pageable
    ) {
        Page<CargaResponse> cargas = buscarCargaService.porCaminhao(codigoCaminhao, pageable);
        return ResponseEntity.ok(cargas);
    }

    @PreAuthorize("hasAuthority('ROLE_MOTORISTA')")
    @GetMapping("/minha-carga-atual")
    public ResponseEntity<CargaResponse> minhaCargaAtual() {
        CargaResponse response = buscarCargaAtualMotoristaService.buscar();
        return ResponseEntity.ok(response);
    }
    //========== INICIAR CARGA ========

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PatchMapping("/iniciar")
    public ResponseEntity<String> iniciarCarga(
            @RequestParam("carga") String numeroCarga,
            @RequestParam("km") Integer kmInicial,
            @RequestParam(value = "ajudantes", required = false) List<String> ajudanteCodigos
    ) {
        String resposta = iniciarCargaService.iniciarCarga(numeroCarga, kmInicial, ajudanteCodigos);
        return ResponseEntity.ok(resposta);
    }

    //========== FINALIZAR CARGA ========

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PatchMapping("/finalizar")
    public ResponseEntity<String> finalizarCarga(
            @RequestParam("carga") String numeroCarga,
            @RequestParam("km") Integer kmfinal
    ) {
        String resposta = finalizarCargaService.finalizarCarga(numeroCarga, kmfinal);
        return ResponseEntity.ok(resposta);
    }


    // ========= CRUD =========

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping
    public ResponseEntity<CargaResponse> registrar(@Valid @RequestBody CargaRequest request) {

        CargaResponse carga = criarCargaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numeroCarga}")
                .buildAndExpand(carga.getNumeroCarga())
                .toUri();

        return ResponseEntity.created(location).body(carga);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/{numeroCarga}")
    public ResponseEntity<CargaResponse> atualizar(@PathVariable String numeroCarga,
                                                   @Valid @RequestBody CargaRequest request) {

        CargaResponse cargaAtualizada = atualizarCargaService.atualizar(numeroCarga, request);
        return ResponseEntity.ok(cargaAtualizada);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @DeleteMapping("/{numeroCarga}")
    public ResponseEntity<Void> deletar(@PathVariable String numeroCarga) {
        deletarCargaService.deletar(numeroCarga);
        return ResponseEntity.noContent().build();
    }
}
