package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.controller.response.VidaUtilPneuResponse;
import br.com.frotasPro.api.repository.TrocaPneuManutencaoRepository;
import br.com.frotasPro.api.service.pneu.*;
import br.com.frotasPro.api.service.relatorios.RelatorioVidaUtilPneuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pneu")
@RequiredArgsConstructor
public class PneuController {

    private final CriarPneuService criarPneuService;
    private final BuscarPneuPorCodigoService buscarPneuPorCodigoService;
    private final ListarPneusService listarPneusService;
    private final ListarPneusPorEixoService listarPneusPorEixoService;
    private final ListarPneusPorCaminhaoService listarPneusPorCaminhaoService;
    private final AtualizarPneuService atualizarPneuService;
    private final DeletarPneuService deletarPneuService;
    private final RelatorioVidaUtilPneuService listarVidaUtilPorPneu;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PostMapping
    public ResponseEntity<PneuResponse> criar(@Valid @RequestBody PneuRequest request) {
        PneuResponse pneu = criarPneuService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(pneu.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(pneu);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/{codigo}")
    public ResponseEntity<PneuResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(buscarPneuPorCodigoService.buscar(codigo));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping
    public ResponseEntity<Page<PneuResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(listarPneusService.listar(pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/eixo/{eixoId}")
    public ResponseEntity<Page<PneuResponse>> listarPorEixo(
            @PathVariable UUID eixoId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                listarPneusPorEixoService.listarPorEixo(eixoId, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}")
    public ResponseEntity<Page<PneuResponse>> listarPorCaminhao(
            @PathVariable String codigoCaminhao,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                listarPneusPorCaminhaoService.listarPorCaminhao(codigoCaminhao, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
    @PutMapping("/{codigo}")
    public ResponseEntity<PneuResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody PneuRequest request
    ) {
        return ResponseEntity.ok(atualizarPneuService.atualizar(codigo, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarPneuService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorios/pneus/vida-util")
    public ResponseEntity<List<VidaUtilPneuResponse>> vidaUtil(
            @RequestParam(required = false) String codigoCaminhao,
            @RequestParam(required = false) String codigoPneu
    ) {
        return ResponseEntity.ok(listarVidaUtilPorPneu.listarVidaUtilPorPneu(codigoCaminhao, codigoPneu));
    }

}
