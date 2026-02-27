package br.com.frotasPro.api.controller.integracao;

import br.com.frotasPro.api.service.integracao.IntegracaoCargaService;
import br.com.frotasPro.api.service.integracao.IntegracaoCaminhaoService;
import br.com.frotasPro.api.service.integracao.IntegracaoMotoristaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integracao")
@RequiredArgsConstructor
public class IntegracaoWinThorController {

    private final IntegracaoMotoristaService motoristaIntegracaoService;
    private final IntegracaoCaminhaoService caminhaoIntegracaoService;
    private final IntegracaoCargaService integracaoCargaService;

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping("/motoristas/sincronizar")
    public ResponseEntity<?> sincronizarMotoristas(
            @RequestParam UUID empresaId,
            @RequestParam(value = "codigosMotoristas", required = false) List<Integer> codigosMotoristas
    ) {
        UUID jobId = motoristaIntegracaoService.solicitarSincronizacao(empresaId, codigosMotoristas);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping("/caminhoes/sincronizar")
    public ResponseEntity<?> sincronizarCaminhoes(
            @RequestParam UUID empresaId,
            @RequestParam(required = false) Integer codFilial,
            @RequestParam(value = "codigosCaminhoes", required = false) List<Integer> codigosCaminhoes
    ) {
        UUID jobId = caminhaoIntegracaoService.solicitarSincronizacao(empresaId, codFilial, codigosCaminhoes);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping("/cargas/sincronizar")
    public ResponseEntity<?> sincronizar(
            @RequestParam("empresaId") UUID empresaId,
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data,
            @RequestParam(value = "dataInicial", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicial,
            @RequestParam(value = "dataFinal", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFinal,
            @RequestParam(value = "codigosCaminhoes", required = false) List<Integer> codigosCaminhoes,
            @RequestParam(value = "codigosMotoristas", required = false) List<Integer> codigosMotoristas,
            @RequestParam(value = "tipoCarga", defaultValue = "FATURADA") String tipoCarga,
            @RequestParam(value = "origem", defaultValue = "API_FROTAPRO") String origem,
            @RequestParam(value = "solicitadoPor", defaultValue = "sistema") String solicitadoPor
    ) {

        LocalDate hoje = LocalDate.now();

        if (dataInicial == null && dataFinal == null) {
            dataInicial = data != null ? data : hoje;
            dataFinal = dataInicial;
        } else {
            if (dataInicial == null) {
                dataInicial = dataFinal;
            }
            if (dataFinal == null) {
                dataFinal = dataInicial;
            }
        }

//        if (data.isBefore(hoje)) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of(
//                            "erro", "A data da sincronização não pode ser inferior a hoje.",
//                            "hoje", hoje.toString()
//                    ));
//        }
//
//        if (data.isAfter(hoje)) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of(
//                            "erro", "A data da sincronização não pode ser futura.",
//                            "hoje", hoje.toString()
//                    ));
//        }

        UUID jobId = integracaoCargaService.solicitarSincronizacao(
                empresaId,
                dataInicial,
                dataFinal,
                codigosCaminhoes,
                codigosMotoristas,
                tipoCarga,
                origem,
                solicitadoPor
        );
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

}
