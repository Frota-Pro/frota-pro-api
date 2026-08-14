package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ParametroSistemaUpdateRequest;
import br.com.frotasPro.api.controller.response.ParametroSistemaResponse;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parametro-sistema")
@RequiredArgsConstructor
public class ParametroSistemaController {

    private final ParametroSistemaService parametroSistemaService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ParametroSistemaResponse> buscar() {
        return ResponseEntity.ok(parametroSistemaService.buscar());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PutMapping
    public ResponseEntity<ParametroSistemaResponse> atualizar(@Valid @RequestBody ParametroSistemaUpdateRequest request) {
        return ResponseEntity.ok(parametroSistemaService.atualizar(request));
    }
}
