package br.com.frotasPro.api.modules.integracao.controller;

import br.com.frotasPro.api.modules.integracao.dto.response.IntegracaoWinThorLogsResponse;
import br.com.frotasPro.api.modules.integracao.service.IntegracaoWinThorLogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integracao/winthor")
@RequiredArgsConstructor
public class IntegracaoWinThorLogsController {

    private final IntegracaoWinThorLogsService logsService;

    @PreAuthorize("hasAuthority('ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/logs")
    public IntegracaoWinThorLogsResponse logs(
            @RequestParam(value = "source", defaultValue = "API") String source,
            @RequestParam(value = "lines", defaultValue = "300") int lines
    ) {
        return logsService.buscarLogs(source, lines);
    }
}
