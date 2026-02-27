package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.OficinaRequest;
import br.com.frotasPro.api.controller.response.OficinaDashboardResponse;
import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.service.oficina.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/oficinas")
@RequiredArgsConstructor
public class OficinaController {

    private final BuscarTodasOficinasService buscarTodasOficinasService;
    private final BuscarOficinaPorCodigoService buscarOficinaPorCodigoService;
    private final CriarOficinaService criarOficinaService;
    private final AtualizarOficinaService atualizarOficinaService;
    private final DeletarOficinaService deletarOficinaService;
    private final OficinaDashboardService oficinaDashboardService;


    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN')")
    @GetMapping
    @Cacheable("oficina_listar")
    public ResponseEntity<Page<OficinaResponse>> listar(Pageable pageable) {
        Page<OficinaResponse> page = buscarTodasOficinasService.listar(pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN')")
    @GetMapping("/codigo/{codigo}")
    @Cacheable("oficina_buscar_codigo")
    public ResponseEntity<OficinaResponse> buscarPorCodigo(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 50, message = "Código inválido")
            String codigo
    ) {
        OficinaResponse response = buscarOficinaPorCodigoService.porCodigo(codigo);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = "oficina_listar", allEntries = true),
            @CacheEvict(value = "oficina_buscar_codigo", allEntries = true),
            @CacheEvict(value = "oficina_dashboard", allEntries = true)
    })
    public ResponseEntity<OficinaResponse> criar(@Valid @RequestBody OficinaRequest request) {
        OficinaResponse response = criarOficinaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PutMapping("/{codigo}")
    @Caching(evict = {
            @CacheEvict(value = "oficina_listar", allEntries = true),
            @CacheEvict(value = "oficina_buscar_codigo", allEntries = true),
            @CacheEvict(value = "oficina_dashboard", allEntries = true)
    })
    public ResponseEntity<OficinaResponse> atualizar(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 50, message = "Código inválido")
            String codigo,
            @Valid @RequestBody OficinaRequest request) {

        OficinaResponse response = atualizarOficinaService.atualizar(codigo, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{codigo}")
    @Caching(evict = {
            @CacheEvict(value = "oficina_listar", allEntries = true),
            @CacheEvict(value = "oficina_buscar_codigo", allEntries = true),
            @CacheEvict(value = "oficina_dashboard", allEntries = true)
    })
    public ResponseEntity<Void> deletar(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 50, message = "Código inválido")
            String codigo
    ) {
        deletarOficinaService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{codigo}/dashboard")
    @Cacheable("oficina_dashboard")
    public ResponseEntity<OficinaDashboardResponse> dashboard(
            @PathVariable
            @NotBlank(message = "Código é obrigatório")
            @Size(max = 50, message = "Código inválido")
            String codigo,
            @RequestParam("inicio")
            @NotNull(message = "Data de início é obrigatória")
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate inicio,
            @RequestParam("fim")
            @NotNull(message = "Data de fim é obrigatória")
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fim
    ) {
        return ResponseEntity.ok(oficinaDashboardService.gerar(codigo, inicio, fim));
    }

}
