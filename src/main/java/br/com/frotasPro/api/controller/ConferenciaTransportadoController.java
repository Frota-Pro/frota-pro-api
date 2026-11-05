package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoService;
import br.com.frotasPro.api.service.conferencia.ZerarCargasTransferidasService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

/** Conferência mensal do valor transportado (WinThor × FrotaPRO), com cada diferença explicada. */
@RestController
@RequestMapping("/conferencia")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA')")
public class ConferenciaTransportadoController {

    private final ConferenciaTransportadoService service;
    private final ZerarCargasTransferidasService zerarService;

    /** Sem "mes" (yyyy-MM) vale o mês corrente. "atualizar=true" ignora o resultado guardado e consulta o WinThor de novo. */
    @GetMapping("/transportado")
    public ResponseEntity<ConferenciaTransportadoResponse> transportado(
            @RequestParam(value = "mes", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @RequestParam(value = "atualizar", defaultValue = "false") boolean atualizar) {
        YearMonth referencia = mes != null ? mes : YearMonth.from(FusoHorarioUtils.hojeBrasil());
        return ResponseEntity.ok(service.gerar(referencia, atualizar));
    }

    /** Cargas que ficaram vazias (notas foram para outras cargas) e podem ser tiradas dos totais. Não muda nada. */
    @GetMapping("/transportado/transferidas")
    public ResponseEntity<List<ZerarCargasTransferidasService.CargaZerada>> transferidas(
            @RequestParam(value = "mes", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return ResponseEntity.ok(zerarService.candidatas(mes != null ? mes : YearMonth.from(FusoHorarioUtils.hojeBrasil())));
    }

    /** Zera dos totais as cargas da lista acima. Só administrador: muda valor e peso gravados (com o antes guardado). */
    @PostMapping("/transportado/zerar-transferidas")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ZerarCargasTransferidasService.CargaZerada>> zerarTransferidas(
            @RequestParam("mes") @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return ResponseEntity.ok(zerarService.aplicar(mes));
    }
}
