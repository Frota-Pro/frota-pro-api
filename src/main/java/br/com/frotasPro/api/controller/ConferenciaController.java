package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.ConferenciaConfirmadaResponse;
import br.com.frotasPro.api.controller.response.ConferenciaMotoristaCaminhaoResponse;
import br.com.frotasPro.api.controller.response.ConferenciaTipoResponse;
import br.com.frotasPro.api.domain.enums.TipoConferencia;
import br.com.frotasPro.api.service.conferencia.ConferenciaMotoristaCaminhaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Tela de Conferência: listas de itens que o sistema acha estranhos e que uma pessoa confere. Ver só exige consulta;
 * confirmar/ignorar exige os mesmos perfis que já trocam motorista e caminhão de uma carga.
 */
@RestController
@RequestMapping("/conferencia")
@RequiredArgsConstructor
public class ConferenciaController {

    private final ConferenciaMotoristaCaminhaoService motoristaCaminhaoService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/tipos")
    public ResponseEntity<List<ConferenciaTipoResponse>> tipos() {
        return ResponseEntity.ok(Arrays.stream(TipoConferencia.values())
                .map(t -> ConferenciaTipoResponse.builder()
                        .codigo(t.name())
                        .titulo(t.getTitulo())
                        .descricao(t.getDescricao())
                        .build())
                .toList());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/reentrega-motorista-caminhao")
    public ResponseEntity<ConferenciaMotoristaCaminhaoResponse> listarReentregaMotoristaCaminhao(
            @RequestParam(value = "inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(motoristaCaminhaoService.listar(inicio, fim));
    }

    /** Aplica na carga o motorista e o caminhão do carregamento (pelas trocas manuais que já existem). */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/reentrega-motorista-caminhao/{numeroCargaExterno}/confirmar-troca")
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
    public ResponseEntity<ConferenciaConfirmadaResponse> confirmarTroca(@PathVariable String numeroCargaExterno) {
        return ResponseEntity.ok(motoristaCaminhaoService.confirmar(numeroCargaExterno));
    }

    /** "Não é isso": o item sai da lista e só volta se o WinThor passar a apontar outro motorista/veículo. */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/reentrega-motorista-caminhao/{numeroCargaExterno}/ignorar-troca")
    public ResponseEntity<Void> ignorarTroca(@PathVariable String numeroCargaExterno) {
        motoristaCaminhaoService.ignorar(numeroCargaExterno);
        return ResponseEntity.noContent().build();
    }
}
