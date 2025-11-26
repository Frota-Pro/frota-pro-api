package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.controller.response.RelatorioManutencaoCaminhaoResponse;
import br.com.frotasPro.api.service.manutencao.*;
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

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
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
    public ResponseEntity<ManutencaoResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(buscarManutencaoPorCodigoService.buscar(codigo));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<ManutencaoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(listarManutencoesPaginadoService.listar(pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{codigo}")
    public ResponseEntity<ManutencaoResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody ManutencaoRequest request
    ) {
        return ResponseEntity.ok(atualizarManutencaoService.atualizar(codigo, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarManutencaoService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}")
    public ResponseEntity<Page<ManutencaoResponse>> buscarPorCaminhao(
            @PathVariable String codigoCaminhao,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarManutencoesPorCaminhaoService.buscarPorCaminhao(codigoCaminhao, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}/periodo")
    public ResponseEntity<Page<ManutencaoResponse>> buscarPorCaminhaoEPeriodo(
            @PathVariable String codigoCaminhao,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarManutencoesPorCaminhaoService.buscarPorCaminhaoEPeriodo(codigoCaminhao, inicio, fim, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/periodo")
    public ResponseEntity<Page<ManutencaoResponse>> buscarPorPeriodo(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarManutencoesPorPeriodoService.buscar(inicio, fim, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/oficina/{codigoOficina}/periodo")
    public ResponseEntity<Page<ManutencaoResponse>> buscarPorOficinaEPeriodo(
            @PathVariable String codigoOficina,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarManutencoesPorOficinaEPeriodoService.buscar(codigoOficina, inicio, fim, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/relatorios/caminhao")
    public ResponseEntity<RelatorioManutencaoCaminhaoResponse> relatorioPorCaminhaoEPeriodo(
            @RequestParam("caminhao") String codigoCaminhao,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(
                relatorioManutencaoCaminhaoService.gerar(codigoCaminhao, inicio, fim)
        );
    }
}
