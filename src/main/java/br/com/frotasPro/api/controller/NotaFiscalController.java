package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.EnviarNotaFiscalEmailRequest;
import br.com.frotasPro.api.controller.response.NotaFiscalResumoResponse;
import br.com.frotasPro.api.service.notafiscal.NotaFiscalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Documentos fiscais (XML/DANFE) de uma carga, buscados na hora no WinThor.
 * Nada fica salvo aqui — só disponível enquanto a carga não for finalizada.
 */
@RestController
@RequestMapping("/carga/{numeroCarga}/notas-fiscais")
@RequiredArgsConstructor
public class NotaFiscalController {

    private final NotaFiscalService notaFiscalService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_MOTORISTA')")
    @GetMapping
    public ResponseEntity<List<NotaFiscalResumoResponse>> listar(
            @PathVariable String numeroCarga,
            @RequestParam String cliente
    ) {
        return ResponseEntity.ok(notaFiscalService.listar(numeroCarga, cliente));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_MOTORISTA')")
    @GetMapping(value = "/{numeroNota}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> xml(@PathVariable String numeroCarga, @PathVariable Long numeroNota) {
        String xml = notaFiscalService.buscarXml(numeroCarga, numeroNota);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"NF_" + numeroNota + ".xml\"")
                .body(xml);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_MOTORISTA')")
    @GetMapping(value = "/{numeroNota}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable String numeroCarga, @PathVariable Long numeroNota) {
        byte[] pdf = notaFiscalService.buscarPdf(numeroCarga, numeroNota);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"NF_" + numeroNota + ".pdf\"")
                .body(pdf);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA', 'ROLE_CONSULTA', 'ROLE_MOTORISTA')")
    @PostMapping("/{numeroNota}/enviar-email")
    public ResponseEntity<Void> enviarEmail(
            @PathVariable String numeroCarga,
            @PathVariable Long numeroNota,
            @Valid @RequestBody EnviarNotaFiscalEmailRequest request
    ) {
        notaFiscalService.enviarPorEmail(numeroCarga, numeroNota, request.getDestinatario());
        return ResponseEntity.noContent().build();
    }
}
