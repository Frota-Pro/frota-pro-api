package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.NotificacaoResponse;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA','ROLE_ADMIN','ROLE_GERENTE_LOGISTICA','ROLE_OPERADOR_LOGISTICA','ROLE_MOTORISTA')")
    @GetMapping
    public ResponseEntity<Page<NotificacaoResponse>> listar(
            @RequestParam(name = "naoLidas", defaultValue = "false") boolean naoLidas,
            Pageable pageable
    ) {
        return ResponseEntity.ok(notificacaoService.listarMinhas(pageable, naoLidas));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA','ROLE_ADMIN','ROLE_GERENTE_LOGISTICA','ROLE_OPERADOR_LOGISTICA','ROLE_MOTORISTA')")
    @GetMapping("/nao-lidas/total")
    public ResponseEntity<Map<String, Long>> totalNaoLidas() {
        long total = notificacaoService.contarNaoLidasMinhas();
        return ResponseEntity.ok(Map.of("total", total));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA','ROLE_ADMIN','ROLE_GERENTE_LOGISTICA','ROLE_OPERADOR_LOGISTICA','ROLE_MOTORISTA')")
    @PatchMapping("/{id}/ler")
    public ResponseEntity<Void> marcarComoLida(@PathVariable UUID id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA','ROLE_ADMIN','ROLE_GERENTE_LOGISTICA','ROLE_OPERADOR_LOGISTICA','ROLE_MOTORISTA')")
    @PatchMapping("/ler-todas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLidas() {
        int total = notificacaoService.marcarTodasComoLidas();
        return ResponseEntity.ok(Map.of("atualizadas", total));
    }
}
