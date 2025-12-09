package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.AnexoParadaResponse;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.enums.TipoAnexoParada;
import br.com.frotasPro.api.domain.enums.TipoParada;
import br.com.frotasPro.api.service.paradaCarga.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parada-carga")
public class ParadaCargaController {

    private final CriarParadaCargaService criarService;
    private final ListarParadaCargaService listarParadaCargaService;
    private final ListarParadaComManutencaoService listarParadaComManutencaoService;
    private final ListarParadaPorTipoService listarParadaPorTipoService;
    private final AtualizarParadaCargaService atualizarService;
    private final DeletarParadaCargaService deletarService;
    private final RegistrarAnexoParadaService registrarAnexoParadaService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PostMapping
    public ResponseEntity<ParadaCargaResponse> registrar(
            @Valid @RequestBody ParadaCargaRequest request) {

        ParadaCargaResponse parada = criarService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(parada.getId())
                .toUri();

        return ResponseEntity.created(location).body(parada);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_CONSULTA')")
    @GetMapping("/carga/{numeroCarga}")
    public ResponseEntity<Page<ParadaCargaResponse>> listarPorCarga(
            @PathVariable String numeroCarga,
            Pageable pageable
    ) {
        Page<ParadaCargaResponse> page =
                listarParadaCargaService.listarPorCarga(numeroCarga, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_CONSULTA')")
    @GetMapping("/carga/{numeroCarga}/tipo")
    public ResponseEntity<Page<ParadaCargaResponse>> listarPorCargaETipo(
            @PathVariable String numeroCarga,
            @RequestParam TipoParada tipoParada,
            Pageable pageable
    ) {
        Page<ParadaCargaResponse> page =
                listarParadaPorTipoService.listarPorCargaETipo(numeroCarga, tipoParada, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_CONSULTA')")
    @GetMapping("/carga/{numeroCarga}/abastecimentos")
    public ResponseEntity<Page<ParadaCargaResponse>> listarAbastecimentosPorCarga(
            @PathVariable String numeroCarga,
            Pageable pageable
    ) {
        Page<ParadaCargaResponse> page =
                listarParadaPorTipoService.listarPorCargaETipo(numeroCarga, TipoParada.ABASTECIMENTO, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_CONSULTA')")
    @GetMapping("/carga/{numeroCarga}/alimentacao")
    public ResponseEntity<Page<ParadaCargaResponse>> listarAlimentacaoPorCarga(
            @PathVariable String numeroCarga,
            Pageable pageable
    ) {
        Page<ParadaCargaResponse> page =
                listarParadaPorTipoService.listarPorCargaETipo(numeroCarga, TipoParada.ALIMENTACAO, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_CONSULTA')")
    @GetMapping("/carga/{numeroCarga}/manutencoes")
    public ResponseEntity<Page<ParadaCargaResponse>> listarParadasComManutencao(
            @PathVariable String numeroCarga,
            Pageable pageable
    ) {
        Page<ParadaCargaResponse> page =
                listarParadaComManutencaoService.listarParadasComManutencao(numeroCarga, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PutMapping("/{id}")
    public ResponseEntity<ParadaCargaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ParadaCargaRequest request) {

        ParadaCargaResponse parada = atualizarService.atualizar(id, request);
        return ResponseEntity.ok(parada);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {

        deletarService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_MOTORISTA')")
    @PostMapping(
            value = "/{paradaId}/anexos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AnexoParadaResponse> uploadAnexoParada(
            @PathVariable UUID paradaId,
            @RequestParam("tipoAnexo") TipoAnexoParada tipoAnexo,
            @RequestParam(value = "observacao", required = false) String observacao,
            @RequestPart("arquivo") MultipartFile arquivo
    ) {
        AnexoParadaResponse response =
                registrarAnexoParadaService.registrar(paradaId, tipoAnexo, observacao, arquivo);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }


}
