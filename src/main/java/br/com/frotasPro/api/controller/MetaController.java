package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MetaRequest;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.service.meta.*;
import br.com.frotasPro.api.util.MetaProgressoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MetaProgressoService metaProgressoService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
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

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
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

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{id}")
    @Cacheable("meta_buscar_id")
    public ResponseEntity<MetaResponse> buscarPorId(@PathVariable UUID id) {
        MetaResponse response = buscarMetaPorIdService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping
    @Cacheable("meta_listar")
    public ResponseEntity<Page<MetaResponse>> listar(Pageable pageable) {
        Page<MetaResponse> page = buscarTodasMetasService.listar(pageable);
        return ResponseEntity.ok(page);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
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

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA','ROLE_ADMIN','ROLE_GERENTE_LOGISTICA','ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/ativas/caminhao/{codigoCaminhao}")
    @Cacheable("meta_ativas_caminhao")
    public ResponseEntity<List<MetaResponse>> metaAtivaCaminhao(
            @PathVariable @NotBlank String codigoCaminhao,
            @RequestParam("dataReferencia")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia
    ) {
        List<MetaResponse> metas = buscarMetaAtivaComProgressoService.buscar(codigoCaminhao, dataReferencia);
        return ResponseEntity.ok(metas);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/historico")
    @Cacheable("meta_historico")
    public ResponseEntity<List<MetaResponse>> historico(
            @RequestParam(required = false) String caminhao,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String motorista,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        return ResponseEntity.ok(buscarHistoricoComProgresso(caminhao, categoria, motorista, inicio, fim));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_CONSULTA')")
    @GetMapping("/historico/caminhao/{codigoCaminhao}")
    @Cacheable("meta_historico_caminhao")
    public ResponseEntity<List<MetaResponse>> historicoPorCaminhao(
            @PathVariable @NotBlank String codigoCaminhao,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(buscarHistoricoComProgresso(codigoCaminhao, null, null, inicio, fim));
    }

    private List<MetaResponse> buscarHistoricoComProgresso(
            String caminhao,
            String categoria,
            String motorista,
            LocalDate inicio,
            LocalDate fim
    ) {
        final Caminhao caminhaoRef = (caminhao != null && !caminhao.isBlank())
                ? caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(caminhao)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o código: " + caminhao))
                : null;

        List<Meta> metas = metaRepository.historicoMetas(caminhao, categoria, motorista, inicio, fim);
        List<MetaResponse> resposta = metas.stream()
                .map(meta -> {
                    if (caminhaoRef != null) {
                        var valorRealizado = metaProgressoService.calcularValorRealizado(meta, caminhaoRef, null);
                        if (valorRealizado != null) {
                            meta.setValorRealizado(valorRealizado);
                        }
                    }
                    return MetaMapper.toResponse(meta);
                })
                .toList();
        return resposta;
    }

}
