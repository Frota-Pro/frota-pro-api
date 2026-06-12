package br.com.frotasPro.api.modules.logistica.controller;

import br.com.frotasPro.api.modules.logistica.dto.request.AtualizarObservacaoMotoristaRequest;
import br.com.frotasPro.api.modules.logistica.dto.request.AtualizarOrdemEntregaRequest;
import br.com.frotasPro.api.modules.logistica.dto.request.CargaRequest;
import br.com.frotasPro.api.modules.logistica.dto.request.MarcarTransferenciaCargaRequest;
import br.com.frotasPro.api.modules.logistica.dto.request.TransferirNotasCargaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.CargaMinResponse;
import br.com.frotasPro.api.modules.logistica.dto.response.CargaResponse;
import br.com.frotasPro.api.modules.logistica.dto.response.TransferirNotasCargaResponse;
import br.com.frotasPro.api.modules.logistica.service.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    private final BuscarCargasFinalizadasMotoristaService buscarCargasFinalizadasMotoristaService;
    private final AtualizarOrdemEntregaService atualizarOrdemEntregaService;
    private final AtualizarObservacaoMotoristaService atualizarObservacaoMotoristaService;
    private final TransferirNotasCargaService transferirNotasCargaService;
    private final MarcarTransferenciaCargaService marcarTransferenciaCargaService;

    // ========= BUSCA ÚNICA =========

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/{numeroCarga}")
    public ResponseEntity<CargaResponse> buscarPorNumero(@PathVariable String numeroCarga) {
        CargaResponse carga = buscarCargaService.porCodigo(numeroCarga);
        return ResponseEntity.ok(carga);
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/externo/{codigoExterno}")
    public ResponseEntity<CargaResponse> buscarPorCodigoExterno(@PathVariable String codigoExterno) {
        CargaResponse carga = buscarCargaService.porCodigoExterno(codigoExterno);
        return ResponseEntity.ok(carga);
    }

    // ========= LISTAGEM GERAL (FILTROS) =========

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<Page<CargaMinResponse>> listar(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        Page<CargaMinResponse> cargas = listarCargaService.listar(q, inicio, fim, pageable);
        return ResponseEntity.ok(cargas);
    }

    // ========= BUSCAS POR DATA =========

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/data-saida")
    public ResponseEntity<Page<CargaResponse>> buscarPorDataSaida(
            @RequestParam("data")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataSaida,
            Pageable pageable
    ) {
        Page<CargaResponse> cargas = buscarCargaService.porDataSaida(dataSaida, pageable);
        return ResponseEntity.ok(cargas);
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
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

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
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

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/motorista")
    public ResponseEntity<Page<CargaResponse>> buscarPorMotorista(
            @RequestParam("codigo") String codigoMotorista,
            Pageable pageable
    ) {
        Page<CargaResponse> cargas = buscarCargaService.porMotorista(codigoMotorista, pageable);
        return ResponseEntity.ok(cargas);
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
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
    public ResponseEntity<List<CargaResponse>> minhaCargaAtual() {
        List<CargaResponse> response = buscarCargaAtualMotoristaService.buscar();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_MOTORISTA')")
    @GetMapping("/minhas-cargas-finalizadas")
    public ResponseEntity<Page<CargaResponse>> minhasCargasFinalizadas(Pageable pageable) {
        Page<CargaResponse> response = buscarCargasFinalizadasMotoristaService.buscar(pageable);
        return ResponseEntity.ok(response);
    }

    //========== INICIAR CARGA ========

    @PreAuthorize("hasAnyAuthority('ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PatchMapping("/iniciar")
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true),
            @CacheEvict(value = "caminhao_listar", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_codigo", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_placa", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "caminhao_detalhes", allEntries = true)
    })
    public ResponseEntity<String> iniciarCarga(
            @RequestParam("carga") String numeroCarga,
            @RequestParam("km") Integer kmInicial,
            @RequestParam(value = "ajudantes", required = false) List<String> ajudanteCodigos
    ) {
        String resposta = iniciarCargaService.iniciarCarga(numeroCarga, kmInicial, ajudanteCodigos);
        return ResponseEntity.ok(resposta);
    }

    //========== FINALIZAR CARGA ========

    @PreAuthorize("hasAnyAuthority('ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PatchMapping("/finalizar")
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true),
            @CacheEvict(value = "caminhao_listar", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_codigo", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_placa", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "caminhao_detalhes", allEntries = true)
    })
    public ResponseEntity<String> finalizarCarga(
            @RequestParam("carga") String numeroCarga,
            @RequestParam("km") Integer kmfinal
    ) {
        String resposta = finalizarCargaService.finalizarCarga(numeroCarga, kmfinal);
        return ResponseEntity.ok(resposta);
    }

    // ========= ORDEM DE ENTREGA / OBSERVAÇÃO MOTORISTA =========

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @PatchMapping("/{numeroCarga}/ordem-entrega")
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    public ResponseEntity<Void> atualizarOrdemEntrega(
            @PathVariable String numeroCarga,
            @Valid @RequestBody AtualizarOrdemEntregaRequest request
    ) {
        atualizarOrdemEntregaService.atualizar(numeroCarga, request.getClientes());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PatchMapping("/{numeroCarga}/observacao")
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    public ResponseEntity<Void> atualizarObservacaoMotorista(
            @PathVariable String numeroCarga,
            @Valid @RequestBody AtualizarObservacaoMotoristaRequest request
    ) {
        atualizarObservacaoMotoristaService.atualizar(numeroCarga, request.getObservacao());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @PatchMapping("/{numeroCarga}/transferir-notas")
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    public ResponseEntity<TransferirNotasCargaResponse> transferirNotas(
            @PathVariable String numeroCarga,
            @Valid @RequestBody TransferirNotasCargaRequest request
    ) {
        TransferirNotasCargaResponse response = transferirNotasCargaService.transferir(numeroCarga, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @PatchMapping("/{numeroCarga}/marcar-transferencia")
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    public ResponseEntity<CargaResponse> marcarTransferencia(
            @PathVariable String numeroCarga,
            @Valid @RequestBody(required = false) MarcarTransferenciaCargaRequest request
    ) {
        CargaResponse response = marcarTransferenciaCargaService.marcar(numeroCarga, request);
        return ResponseEntity.ok(response);
    }

    // ========= CRUD =========

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    public ResponseEntity<CargaResponse> registrar(@Valid @RequestBody CargaRequest request) {

        CargaResponse carga = criarCargaService.criar(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{numeroCarga}")
                .buildAndExpand(carga.getNumeroCarga())
                .toUri();

        return ResponseEntity.created(uri).body(carga);
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/{numeroCarga}")
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    public ResponseEntity<CargaResponse> atualizar(
            @PathVariable String numeroCarga,
            @Valid @RequestBody CargaRequest request
    ) {
        CargaResponse carga = atualizarCargaService.atualizar(numeroCarga, request);
        return ResponseEntity.ok(carga);
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @DeleteMapping("/{numeroCarga}")
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    public ResponseEntity<Void> deletar(@PathVariable String numeroCarga) {
        deletarCargaService.deletar(numeroCarga);
        return ResponseEntity.noContent().build();
    }
}
