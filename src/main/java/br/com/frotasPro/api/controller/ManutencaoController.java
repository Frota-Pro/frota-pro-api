package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.response.DocumentoManutencaoResponse;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.controller.response.RelatorioManutencaoCaminhaoResponse;
import br.com.frotasPro.api.domain.enums.TipoDocumentoManutencao;
import br.com.frotasPro.api.service.manutencao.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/manutencao")
@AllArgsConstructor
public class ManutencaoController {

    private final CriarManutencaoService criarManutencaoService;
    private final AtualizarManutencaoService atualizarManutencaoService;
    private final DeletarManutencaoService deletarManutencaoService;
    private final BuscarManutencaoPorCodigoService buscarManutencaoPorCodigoService;
    private final ListarManutencoesPaginadoService listarManutencoesPaginadoService;
    private final BuscarManutencoesPorCaminhaoService buscarManutencoesPorCaminhaoService;
    private final BuscarManutencoesPorPeriodoService buscarManutencoesPorPeriodoService;
    private final BuscarManutencoesPorOficinaEPeriodoService buscarManutencoesPorOficinaEPeriodoService;
    private final RelatorioManutencaoCaminhaoService relatorioManutencaoCaminhaoService;

    private final RegistrarDocumentoManutencaoService registrarDocumentoManutencaoService;
    private final ListarDocumentoManutencaoService listarDocumentoManutencaoService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = "manutencao_buscar_codigo", allEntries = true),
            @CacheEvict(value = "manutencao_listar", allEntries = true),
            @CacheEvict(value = "manutencao_caminhao", allEntries = true),
            @CacheEvict(value = "manutencao_caminhao_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_oficina_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_relatorio_caminhao", allEntries = true),
            @CacheEvict(value = "manutencao_documentos", allEntries = true)
    })
    public ResponseEntity<ManutencaoResponse> criar(@Valid @RequestBody ManutencaoRequest request) {
        ManutencaoResponse manutencao = criarManutencaoService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(manutencao.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(manutencao);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/{codigo}")
    @Cacheable("manutencao_buscar_codigo")
    public ResponseEntity<ManutencaoResponse> buscarPorCodigo(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 50, message = "Código inválido")
            String codigo
    ) {
        return ResponseEntity.ok(buscarManutencaoPorCodigoService.buscar(codigo));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping
    @Cacheable("manutencao_listar")
    public ResponseEntity<Page<ManutencaoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(listarManutencoesPaginadoService.listar(pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{codigo}")
    @Caching(evict = {
            @CacheEvict(value = "manutencao_buscar_codigo", allEntries = true),
            @CacheEvict(value = "manutencao_listar", allEntries = true),
            @CacheEvict(value = "manutencao_caminhao", allEntries = true),
            @CacheEvict(value = "manutencao_caminhao_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_oficina_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_relatorio_caminhao", allEntries = true),
            @CacheEvict(value = "manutencao_documentos", allEntries = true)
    })
    public ResponseEntity<ManutencaoResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody ManutencaoRequest request
    ) {
        return ResponseEntity.ok(atualizarManutencaoService.atualizar(codigo, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{codigo}")
    @Caching(evict = {
            @CacheEvict(value = "manutencao_buscar_codigo", allEntries = true),
            @CacheEvict(value = "manutencao_listar", allEntries = true),
            @CacheEvict(value = "manutencao_caminhao", allEntries = true),
            @CacheEvict(value = "manutencao_caminhao_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_oficina_periodo", allEntries = true),
            @CacheEvict(value = "manutencao_relatorio_caminhao", allEntries = true),
            @CacheEvict(value = "manutencao_documentos", allEntries = true)
    })
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarManutencaoService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}")
    @Cacheable("manutencao_caminhao")
    public ResponseEntity<Page<ManutencaoResponse>> buscarPorCaminhao(
            @PathVariable
            @NotBlank(message = "Código do caminhão é obrigatório")
            @Size(max = 50, message = "Código do caminhão inválido")
            String codigoCaminhao,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarManutencoesPorCaminhaoService.buscarPorCaminhao(codigoCaminhao, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}/periodo")
    @Cacheable("manutencao_caminhao_periodo")
    public ResponseEntity<Page<ManutencaoResponse>> buscarPorCaminhaoEPeriodo(
            @PathVariable
            @NotBlank(message = "Código do caminhão é obrigatório")
            @Size(max = 50, message = "Código do caminhão inválido")
            String codigoCaminhao,
            @RequestParam("inicio")
            @NotNull(message = "Data de início é obrigatória")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")
            @NotNull(message = "Data de fim é obrigatória")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarManutencoesPorCaminhaoService.buscarPorCaminhaoEPeriodo(codigoCaminhao, inicio, fim, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/periodo")
    @Cacheable("manutencao_periodo")
    public ResponseEntity<Page<ManutencaoResponse>> buscarPorPeriodo(
            @RequestParam("inicio")
            @NotNull(message = "Data de início é obrigatória")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")
            @NotNull(message = "Data de fim é obrigatória")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarManutencoesPorPeriodoService.buscar(inicio, fim, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/oficina/{codigoOficina}/periodo")
    @Cacheable("manutencao_oficina_periodo")
    public ResponseEntity<Page<ManutencaoResponse>> buscarPorOficinaEPeriodo(
            @PathVariable
            @NotBlank(message = "Código da oficina é obrigatório")
            @Size(max = 50, message = "Código da oficina inválido")
            String codigoOficina,
            @RequestParam("inicio")
            @NotNull(message = "Data de início é obrigatória")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")
            @NotNull(message = "Data de fim é obrigatória")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarManutencoesPorOficinaEPeriodoService.buscar(codigoOficina, inicio, fim, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/relatorios/caminhao")
    @Cacheable("manutencao_relatorio_caminhao")
    public ResponseEntity<RelatorioManutencaoCaminhaoResponse> relatorioPorCaminhaoEPeriodo(
            @RequestParam("caminhao")
            @NotBlank(message = "Código do caminhão é obrigatório")
            @Size(max = 50, message = "Código do caminhão inválido")
            String codigoCaminhao,
            @RequestParam("inicio")
            @NotNull(message = "Data de início é obrigatória")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")
            @NotNull(message = "Data de fim é obrigatória")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(
                relatorioManutencaoCaminhaoService.gerar(codigoCaminhao, inicio, fim)
        );
    }

    // =========================
    // DOCUMENTOS / ANEXOS
    // =========================

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping(
            value = "/{codigo}/documentos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @CacheEvict(value = "manutencao_documentos", allEntries = true)
    public ResponseEntity<DocumentoManutencaoResponse> uploadDocumentoManutencao(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 50, message = "Código inválido")
            String codigo,
            @RequestParam("tipoDocumento")
            @NotNull(message = "Tipo de documento é obrigatório")
            TipoDocumentoManutencao tipoDocumento,
            @RequestParam(value = "observacao", required = false) String observacao,
            @RequestPart("arquivo") MultipartFile arquivo
    ) {
        DocumentoManutencaoResponse response =
                registrarDocumentoManutencaoService.registrar(codigo, tipoDocumento, observacao, arquivo);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{codigo}/documentos")
    @Cacheable("manutencao_documentos")
    public ResponseEntity<Page<DocumentoManutencaoResponse>> listarDocumentosManutencao(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 50, message = "Código inválido")
            String codigo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(listarDocumentoManutencaoService.listarPorManutencao(codigo, pageable));
    }
}
