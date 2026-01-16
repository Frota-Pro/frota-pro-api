package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.AbastecimentoRequest;
import br.com.frotasPro.api.controller.response.AbastecimentoGastoPorCombustivelResponse;
import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.controller.response.AbastecimentoResumoCaminhaoResponse;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.service.abastecimento.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

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

    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\',)")
    @GetMapping("/{codigo}")
    public ResponseEntity<AbastecimentoResponse> buscarPorCodigo(@PathVariable String codigo) {
        AbastecimentoResponse abastecimento = buscarAbastecimentoPorCodigoService.buscar(codigo);
        return ResponseEntity.ok(abastecimento);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\',)")
    @GetMapping
    public ResponseEntity<Page<AbastecimentoResponse>> listar(Pageable pageable) {
        Page<AbastecimentoResponse> ajudantes = listarService.listar(pageable);
        return ResponseEntity.ok(ajudantes);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\',)")
    @GetMapping("/periodo")
    public ResponseEntity<Page<AbastecimentoResponse>> buscarPorPeriodo(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fim,

            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarPorPeriodoService.buscar(inicio, fim, pageable)
        );
    }

    @GetMapping("/periodo/combustivel")
    public ResponseEntity<Page<AbastecimentoResponse>> buscarPorTipoCombustivel(
            @RequestParam TipoCombustivel tipo,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarPorCombustivelPeriodoService.buscar(tipo, inicio, fim, pageable)
        );
    }

    @GetMapping("/periodo/formapagamento")
    public ResponseEntity<Page<AbastecimentoResponse>> buscarPorFormaPagamento(
            @RequestParam FormaPagamento forma,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarPorFormaPagamentoPeriodoService.buscar(forma, inicio, fim, pageable)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping
    public ResponseEntity<AbastecimentoResponse> criar(@Valid @RequestBody AbastecimentoRequest request) {

        AbastecimentoResponse response = criarService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(response.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/{codigo}")
    public ResponseEntity<AbastecimentoResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody AbastecimentoRequest request) {

        AbastecimentoResponse response = atualizarService.atualizar(codigo, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/relatorios/gasto-por-combustivel")
    public ResponseEntity<List<AbastecimentoGastoPorCombustivelResponse>> gastoPorCombustivel(
            @RequestParam("inicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        List<AbastecimentoGastoPorCombustivelResponse> relatorio =
                relatorioService.gastoPorCombustivel(inicio, fim);

        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/relatorio/resumo-caminhao")
    public ResponseEntity<List<AbastecimentoResumoCaminhaoResponse>> resumoPorCaminhao(
            @RequestParam("inicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,

            @RequestParam("fim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(
                resumoPorCaminhaoService.gerar(inicio, fim)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA','ROLE_ADMIN','ROLE_GERENTE_LOGISTICA','ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/caminhao")
    public ResponseEntity<Page<AbastecimentoResponse>> buscarPorCaminhao(
            @RequestParam("codigo") String codigoCaminhao,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                buscarAbastecimentosPorCaminhaoService.buscar(codigoCaminhao, pageable)
        );
    }
}
