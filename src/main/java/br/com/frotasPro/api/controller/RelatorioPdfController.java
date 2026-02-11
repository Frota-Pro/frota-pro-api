package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.*;
import br.com.frotasPro.api.service.relatorios.*;
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

    private final JasperPdfService jasperPdfService;

    private final RelatorioMetaMensalMotoristaService relatorioService;
    private final RelatorioCustoPorCaminhaoService custoPorCaminhaoService;
    private final RelatorioAbastecimentoPeriodoService abastecimentoPeriodoService;
    private final RelatorioCargaCompletaService cargaCompletaService;
    private final RelatorioRankingMotoristasService rankingMotoristasService;
    private final RelatorioHistoricoManutencaoService historicoManutencaoService;
    private final RelatorioVidaUtilPneuService vidaUtilPneuService;

    private static final String LOGO_CLASSPATH = "reports/logo.png";

    private void aplicarLogo(Map<String, Object> p) {
        // JRXML usa $P{logoPath}
        p.put("logoPath", this.getClass().getClassLoader().getResource(LOGO_CLASSPATH));
    }


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
        aplicarLogo(p);

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

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}/custo")
    public ResponseEntity<byte[]> custoPorCaminhaoPdf(
            @PathVariable String codigoCaminhao,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        RelatorioCustoPorCaminhaoResponse rel = custoPorCaminhaoService.gerar(codigoCaminhao, inicio, fim);

        Map<String, Object> p = new HashMap<>();
        p.put("codigoCaminhao", rel.getCodigoCaminhao());
        p.put("placaCaminhao", rel.getPlacaCaminhao());
        p.put("periodoInicio", rel.getPeriodoInicio());
        p.put("periodoFim", rel.getPeriodoFim());
        p.put("totalCombustivel", rel.getTotalCombustivel());
        p.put("totalManutencao", rel.getTotalManutencao());
        p.put("totalGeral", rel.getTotalGeral());
        aplicarLogo(p);

        byte[] pdf = jasperPdfService.gerarPdfFromJrxml(
                "reports/custo_por_caminhao.jrxml",
                p,
                rel.getLinhas()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"custo-por-caminhao-" + codigoCaminhao + ".pdf\"")
                .body(pdf);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/abastecimentos")
    public ResponseEntity<byte[]> abastecimentoPeriodoPdf(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(value = "codigoCaminhao", required = false) String codigoCaminhao,
            @RequestParam(value = "codigoMotorista", required = false) String codigoMotorista
    ) {
        RelatorioAbastecimentoPeriodoResponse rel = abastecimentoPeriodoService.gerar(inicio, fim, codigoCaminhao, codigoMotorista);

        Map<String, Object> p = new HashMap<>();
        p.put("periodoInicio", rel.getPeriodoInicio());
        p.put("periodoFim", rel.getPeriodoFim());
        p.put("filtroCaminhao", rel.getFiltroCaminhao());
        p.put("filtroMotorista", rel.getFiltroMotorista());
        p.put("totalLitros", rel.getTotalLitros());
        p.put("totalValor", rel.getTotalValor());
        aplicarLogo(p);

        byte[] pdf = jasperPdfService.gerarPdfFromJrxml(
                "reports/abastecimento_periodo.jrxml",
                p,
                rel.getLinhas()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"abastecimentos-" + inicio + "-a-" + fim + ".pdf\"")
                .body(pdf);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/carga/{numeroCarga}/completo")
    public ResponseEntity<byte[]> cargaCompletaPdf(@PathVariable String numeroCarga) {
        RelatorioCargaCompletaResponse rel = cargaCompletaService.gerar(numeroCarga);

        Map<String, Object> p = new HashMap<>();
        p.put("numeroCarga", rel.getNumeroCarga());
        p.put("codigoCarga", rel.getCodigoCarga());
        p.put("statusCarga", rel.getStatusCarga());
        p.put("motorista", rel.getMotorista());
        p.put("caminhao", rel.getCaminhao());
        p.put("rota", rel.getRota());
        p.put("dataSaida", rel.getDataSaida());
        p.put("dataChegada", rel.getDataChegada());
        p.put("valorTotal", rel.getValorTotal());
        p.put("pesoCarga", rel.getPesoCarga());
        p.put("observacaoMotorista", rel.getObservacaoMotorista());
        aplicarLogo(p);

        byte[] pdf = jasperPdfService.gerarPdfFromJrxml(
                "reports/carga_completa.jrxml",
                p,
                rel.getLinhas() // linhas “achatadas” (nota/parada) para tabela
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"carga-completa-" + numeroCarga + ".pdf\"")
                .body(pdf);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/motoristas/ranking")
    public ResponseEntity<byte[]> rankingMotoristasPdf(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        RelatorioRankingMotoristasResponse rel = rankingMotoristasService.gerar(inicio, fim);

        Map<String, Object> p = new HashMap<>();
        p.put("periodoInicio", rel.getPeriodoInicio());
        p.put("periodoFim", rel.getPeriodoFim());
        p.put("totalMotoristas", rel.getTotalMotoristas());
        aplicarLogo(p);

        byte[] pdf = jasperPdfService.gerarPdfFromJrxml(
                "reports/ranking_motoristas.jrxml",
                p,
                rel.getLinhas()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"ranking-motoristas-" + inicio + "-a-" + fim + ".pdf\"")
                .body(pdf);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/caminhao/{codigoCaminhao}/manutencoes")
    public ResponseEntity<byte[]> historicoManutencaoPdf(
            @PathVariable String codigoCaminhao,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        RelatorioHistoricoManutencaoResponse rel = historicoManutencaoService.gerar(codigoCaminhao, inicio, fim);

        Map<String, Object> p = new HashMap<>();
        p.put("codigoCaminhao", rel.getCodigoCaminhao());
        p.put("placaCaminhao", rel.getPlacaCaminhao());
        p.put("periodoInicio", rel.getPeriodoInicio());
        p.put("periodoFim", rel.getPeriodoFim());
        p.put("totalManutencao", rel.getTotalManutencao());
        aplicarLogo(p);

        byte[] pdf = jasperPdfService.gerarPdfFromJrxml(
                "reports/historico_manutencao.jrxml",
                p,
                rel.getLinhas()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"manutencoes-" + codigoCaminhao + "-" + inicio + "-a-" + fim + ".pdf\"")
                .body(pdf);
    }

//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
//    @GetMapping("/pneus/vida-util")
//    public ResponseEntity<byte[]> vidaUtilPneuPdf(
//            @RequestParam(value = "codigoCaminhao", required = false) String codigoCaminhao
//    ) {
//        RelatorioVidaUtilPneuResponse rel = vidaUtilPneuService.gerar(codigoCaminhao);
//
//        Map<String, Object> p = new HashMap<>();
//        p.put("filtroCaminhao", rel.getFiltroCaminhao());
//        p.put("totalPneus", rel.getTotalPneus());
//        aplicarLogo(p);
//
//        byte[] pdf = jasperPdfService.gerarPdfFromJrxml(
//                "reports/vida_util_pneu.jrxml",
//                p,
//                rel.getLinhas()
//        );
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_PDF)
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "inline; filename=\"vida-util-pneu.pdf\"")
//                .body(pdf);
//    }
}
