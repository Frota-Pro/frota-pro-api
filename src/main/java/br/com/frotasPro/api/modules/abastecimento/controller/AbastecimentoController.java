package br.com.frotasPro.api.modules.abastecimento.controller;

import br.com.frotasPro.api.modules.abastecimento.dto.request.AbastecimentoRequest;
import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoGastoPorCombustivelResponse;
import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResponse;
import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResumoCaminhaoResponse;
import br.com.frotasPro.api.modules.abastecimento.service.*;
import br.com.frotasPro.api.shared.enums.FormaPagamento;
import br.com.frotasPro.api.shared.enums.TipoCombustivel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/abastecimento")
public class AbastecimentoController {

    private final CriarAbastecimentoService criarService;
    private final BuscarAbastecimentoPorCodigoService buscarAbastecimentoPorCodigoService;
    private final ListarAbastecimentosService listarService;
    private final BuscarAbastecimentoPorCombustivelPeriodoService buscarPorCombustivelPeriodoService;
    private final BuscarAbastecimentoPorFormaPagamentoPeriodoService buscarPorFormaPagamentoPeriodoService;
    private final BuscarAbastecimentoPorPeriodoService buscarPorPeriodoService;
    private final AbastecimentoUpdateService atualizarService;
    private final DeletarAbastecimentoService deletarService;
    private final RelatorioAbastecimentoService relatorioService;
    private final ResumoAbastecimentoPorCaminhaoService resumoPorCaminhaoService;
    private final BuscarAbastecimentosPorCaminhaoService buscarAbastecimentosPorCaminhaoService;
    private final BuscarAbastecimentosFiltradoService buscarAbastecimentosFiltradoService;

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/{codigo}")
    public ResponseEntity<AbastecimentoResponse> buscarPorCodigo(@PathVariable String codigo) {
        AbastecimentoResponse abastecimento = buscarAbastecimentoPorCodigoService.buscar(codigo);
        return ResponseEntity.ok(abastecimento);
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<Page<AbastecimentoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(listarService.listar(pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/filtrar")
    public ResponseEntity<Page<AbastecimentoResponse>> filtrar(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "caminhao", required = false) String caminhao,
            @RequestParam(value = "motorista", required = false) String motorista,
            @RequestParam(value = "tipo", required = false) TipoCombustivel tipo,
            @RequestParam(value = "forma", required = false) FormaPagamento forma,
            @RequestParam(value = "inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(value = "fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarAbastecimentosFiltradoService.buscar(q, caminhao, motorista, tipo, forma, inicio, fim, pageable)
        );
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/periodo")
    public ResponseEntity<Page<AbastecimentoResponse>> buscarPorPeriodo(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(buscarPorPeriodoService.buscar(inicio, fim, pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/periodo/combustivel")
    public ResponseEntity<Page<AbastecimentoResponse>> buscarPorTipoCombustivel(
            @RequestParam TipoCombustivel tipo,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(buscarPorCombustivelPeriodoService.buscar(tipo, inicio, fim, pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/periodo/formapagamento")
    public ResponseEntity<Page<AbastecimentoResponse>> buscarPorFormaPagamento(
            @RequestParam FormaPagamento forma,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(buscarPorFormaPagamentoPeriodoService.buscar(forma, inicio, fim, pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = "abastecimento_buscar_codigo", allEntries = true),
            @CacheEvict(value = "abastecimento_listar", allEntries = true),
            @CacheEvict(value = "abastecimento_filtrar", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo_combustivel", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo_forma", allEntries = true),
            @CacheEvict(value = "abastecimento_relatorio_gasto", allEntries = true),
            @CacheEvict(value = "abastecimento_relatorio_resumo", allEntries = true),
            @CacheEvict(value = "abastecimento_caminhao", allEntries = true),
            @CacheEvict(value = "meta_buscar_id", allEntries = true),
            @CacheEvict(value = "meta_listar", allEntries = true),
            @CacheEvict(value = "meta_ativas_caminhao", allEntries = true),
            @CacheEvict(value = "meta_historico", allEntries = true),
            @CacheEvict(value = "meta_historico_caminhao", allEntries = true)
    })
    public ResponseEntity<AbastecimentoResponse> criar(@Valid @RequestBody AbastecimentoRequest request) {
        AbastecimentoResponse response = criarService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(response.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/{codigo}")
    @Caching(evict = {
            @CacheEvict(value = "abastecimento_buscar_codigo", allEntries = true),
            @CacheEvict(value = "abastecimento_listar", allEntries = true),
            @CacheEvict(value = "abastecimento_filtrar", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo_combustivel", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo_forma", allEntries = true),
            @CacheEvict(value = "abastecimento_relatorio_gasto", allEntries = true),
            @CacheEvict(value = "abastecimento_relatorio_resumo", allEntries = true),
            @CacheEvict(value = "abastecimento_caminhao", allEntries = true),
            @CacheEvict(value = "meta_buscar_id", allEntries = true),
            @CacheEvict(value = "meta_listar", allEntries = true),
            @CacheEvict(value = "meta_ativas_caminhao", allEntries = true),
            @CacheEvict(value = "meta_historico", allEntries = true),
            @CacheEvict(value = "meta_historico_caminhao", allEntries = true)
    })
    public ResponseEntity<AbastecimentoResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody AbastecimentoRequest request
    ) {
        return ResponseEntity.ok(atualizarService.atualizar(codigo, request));
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @DeleteMapping("/{codigo}")
    @Caching(evict = {
            @CacheEvict(value = "abastecimento_buscar_codigo", allEntries = true),
            @CacheEvict(value = "abastecimento_listar", allEntries = true),
            @CacheEvict(value = "abastecimento_filtrar", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo_combustivel", allEntries = true),
            @CacheEvict(value = "abastecimento_periodo_forma", allEntries = true),
            @CacheEvict(value = "abastecimento_relatorio_gasto", allEntries = true),
            @CacheEvict(value = "abastecimento_relatorio_resumo", allEntries = true),
            @CacheEvict(value = "abastecimento_caminhao", allEntries = true),
            @CacheEvict(value = "meta_buscar_id", allEntries = true),
            @CacheEvict(value = "meta_listar", allEntries = true),
            @CacheEvict(value = "meta_ativas_caminhao", allEntries = true),
            @CacheEvict(value = "meta_historico", allEntries = true),
            @CacheEvict(value = "meta_historico_caminhao", allEntries = true)
    })
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/relatorios/gasto-por-combustivel")
    public ResponseEntity<List<AbastecimentoGastoPorCombustivelResponse>> gastoPorCombustivel(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(relatorioService.gastoPorCombustivel(inicio, fim));
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/relatorio/resumo-caminhao")
    public ResponseEntity<List<AbastecimentoResumoCaminhaoResponse>> resumoPorCaminhao(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(resumoPorCaminhaoService.gerar(inicio, fim));
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/caminhao")
    public ResponseEntity<Page<AbastecimentoResponse>> buscarPorCaminhao(
            @RequestParam("codigo") String codigoCaminhao,
            Pageable pageable
    ) {
        return ResponseEntity.ok(buscarAbastecimentosPorCaminhaoService.buscar(codigoCaminhao, pageable));
    }
}
