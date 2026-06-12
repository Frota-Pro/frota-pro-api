package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MetaRequest;
import br.com.frotasPro.api.controller.response.MetaCategoriaDesempenhoResponse;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.controller.response.RelatorioDesempenhoMetasResponse;
import br.com.frotasPro.api.controller.response.TipoMetaRegraResponse;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.service.meta.*;
import br.com.frotasPro.api.service.relatorios.RelatorioDesempenhoMetasService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/metas")
@RequiredArgsConstructor
public class MetaController {

    private final CriarMetaService criarMetaService;
    private final AtualizarMetaService atualizarMetaService;
    private final BuscarMetaPorIdService buscarMetaPorIdService;
    private final BuscarTodasMetasService buscarTodasMetasService;
    private final DeletarMetaService deletarMetaService;
    private final BuscarMetaAtivaComProgressoService buscarMetaAtivaComProgressoService;
    private final BuscarHistoricoMetaComProgressoService buscarHistoricoMetaComProgressoService;
    private final BuscarDesempenhoMetaCategoriaService buscarDesempenhoMetaCategoriaService;
    private final RelatorioDesempenhoMetasService relatorioDesempenhoMetasService;

    @PreAuthorize("hasAuthority('ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = "meta_buscar_id", allEntries = true),
            @CacheEvict(value = "meta_listar", allEntries = true),
            @CacheEvict(value = "meta_ativas_caminhao", allEntries = true),
            @CacheEvict(value = "meta_historico", allEntries = true),
            @CacheEvict(value = "meta_historico_caminhao", allEntries = true)
    })
    public ResponseEntity<MetaResponse> criar(@Valid @RequestBody MetaRequest request) {

        MetaResponse response = criarMetaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAuthority('ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{id}")
    @Caching(evict = {
            @CacheEvict(value = "meta_buscar_id", allEntries = true),
            @CacheEvict(value = "meta_listar", allEntries = true),
            @CacheEvict(value = "meta_ativas_caminhao", allEntries = true),
            @CacheEvict(value = "meta_historico", allEntries = true),
            @CacheEvict(value = "meta_historico_caminhao", allEntries = true)
    })
    public ResponseEntity<MetaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody MetaRequest request) {

        MetaResponse response = atualizarMetaService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{id}")
    public ResponseEntity<MetaResponse> buscarPorId(@PathVariable UUID id) {
        MetaResponse response = buscarMetaPorIdService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<MetaResponse>> listar(Pageable pageable) {
        Page<MetaResponse> page = buscarTodasMetasService.listar(pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/tipos")
    public ResponseEntity<List<TipoMetaRegraResponse>> tipos() {
        return ResponseEntity.ok(
                java.util.Arrays.stream(TipoMeta.values())
                        .map(tipo -> TipoMetaRegraResponse.builder()
                                .tipoMeta(tipo)
                                .descricao(tipo.getDescricao())
                                .regraAtingimento(tipo.getRegraAtingimento())
                                .regraAtingimentoTexto(regraTexto(tipo.getRegraAtingimento()))
                                .build())
                        .toList()
        );
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/desempenho")
    public ResponseEntity<RelatorioDesempenhoMetasResponse> desempenho(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(value = "tipoMeta", required = false) TipoMeta tipoMeta,
            @RequestParam(value = "caminhao", required = false) String caminhao,
            @RequestParam(value = "motorista", required = false) String motorista,
            @RequestParam(value = "categoria", required = false) String categoria
    ) {
        return ResponseEntity.ok(relatorioDesempenhoMetasService.gerar(
                inicio,
                fim,
                tipoMeta,
                caminhao,
                motorista,
                categoria
        ));
    }


    @PreAuthorize("hasAuthority('ROLE_GERENTE_LOGISTICA')")
    @DeleteMapping("/{id}")
    @Caching(evict = {
            @CacheEvict(value = "meta_buscar_id", allEntries = true),
            @CacheEvict(value = "meta_listar", allEntries = true),
            @CacheEvict(value = "meta_ativas_caminhao", allEntries = true),
            @CacheEvict(value = "meta_historico", allEntries = true),
            @CacheEvict(value = "meta_historico_caminhao", allEntries = true)
    })
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        deletarMetaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/ativas/caminhao/{codigoCaminhao}")
    public ResponseEntity<List<MetaResponse>> metaAtivaCaminhao(
            @PathVariable @NotBlank String codigoCaminhao,
            @RequestParam("dataReferencia")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia
    ) {
        List<MetaResponse> metas = buscarMetaAtivaComProgressoService.buscar(codigoCaminhao, dataReferencia);
        return ResponseEntity.ok(metas);
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/categorias/{codigoCategoria}/desempenho")
    public ResponseEntity<MetaCategoriaDesempenhoResponse> desempenhoCategoria(
            @PathVariable @NotBlank String codigoCategoria,
            @RequestParam(value = "dataReferencia", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia,
            @RequestParam(value = "inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        if (inicio != null || fim != null) {
            if (inicio == null || fim == null) {
                throw new IllegalArgumentException("Informe inicio e fim para consultar por período.");
            }
            return ResponseEntity.ok(buscarDesempenhoMetaCategoriaService.buscarPorPeriodo(codigoCategoria, inicio, fim));
        }
        if (dataReferencia == null) {
            throw new IllegalArgumentException("Informe dataReferencia ou inicio/fim.");
        }
        return ResponseEntity.ok(buscarDesempenhoMetaCategoriaService.buscar(codigoCategoria, dataReferencia));
    }


    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/historico")
    public ResponseEntity<List<MetaResponse>> historico(
            @RequestParam(required = false) String caminhao,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String motorista,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        return ResponseEntity.ok(buscarHistoricoMetaComProgressoService.historico(caminhao, categoria, motorista, inicio, fim));
    }

    @PreAuthorize("hasAuthority('ROLE_CONSULTA')")
    @GetMapping("/historico/caminhao/{codigoCaminhao}")
    public ResponseEntity<List<MetaResponse>> historicoPorCaminhao(
            @PathVariable @NotBlank String codigoCaminhao,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(buscarHistoricoMetaComProgressoService.historicoPorCaminhao(codigoCaminhao, inicio, fim));
    }

    private String regraTexto(String regra) {
        if ("MENOR_OU_IGUAL".equals(regra)) {
            return "Menor ou igual a meta";
        }
        if ("MAIOR_OU_IGUAL".equals(regra)) {
            return "Maior ou igual a meta";
        }
        return regra;
    }

}
