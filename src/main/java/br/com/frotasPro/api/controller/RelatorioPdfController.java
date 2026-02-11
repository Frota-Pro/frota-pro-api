package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.RelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.service.relatorios.JasperPdfService;
import br.com.frotasPro.api.service.relatorios.RelatorioMetaMensalMotoristaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/relatorios/pdf")
@RequiredArgsConstructor
public class RelatorioPdfController {

    private final RelatorioMetaMensalMotoristaService relatorioService;
    private final JasperPdfService jasperPdfService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/motorista/{codigoMotorista}/meta-mensal")
    public ResponseEntity<byte[]> metaMensalMotoristaPdf(
            @PathVariable String codigoMotorista,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        RelatorioMetaMensalMotoristaResponse rel = relatorioService.gerar(codigoMotorista, inicio, fim);

        Map<String, Object> p = new HashMap<>();
        p.put("nomeMotorista", rel.getNomeMotorista());
        p.put("codigoMotorista", rel.getCodigoMotorista());
        p.put("placaCaminhao", rel.getPlacaCaminhao());
        p.put("codigoCaminhao", rel.getCodigoCaminhao());
        p.put("periodoInicio", rel.getPeriodoInicio());
        p.put("periodoFim", rel.getPeriodoFim());
        p.put("objetivoMesTonelada", rel.getObjetivoMesTonelada());
        p.put("metaConsumoKmPorLitro", rel.getMetaConsumoKmPorLitro());

        p.put("totalTonelada", rel.getTotalTonelada());
        p.put("totalKmRodado", rel.getTotalKmRodado());
        p.put("totalLitros", rel.getTotalLitros());
        p.put("totalValorAbastecimento", rel.getTotalValorAbastecimento());
        p.put("mediaGeralKmPorLitro", rel.getMediaGeralKmPorLitro());
        p.put("realizadoToneladaPercentual", rel.getRealizadoToneladaPercentual());

        // Logo (opcional). Se não for usar, remova do JRXML e daqui também.
        p.put("logoPath", this.getClass().getResource("/reports/logo.png"));

        byte[] pdf = jasperPdfService.gerarPdfFromJrxml(
                "reports/meta_mensal_motorista.jrxml",
                p,
                rel.getLinhas()
        );

        String filename = "meta-mensal-motorista-" + codigoMotorista + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
