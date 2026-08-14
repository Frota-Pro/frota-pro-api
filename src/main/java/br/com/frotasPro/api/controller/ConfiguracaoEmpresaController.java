package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ConfiguracaoEmpresaUpdateRequest;
import br.com.frotasPro.api.controller.response.ConfiguracaoEmpresaResponse;
import br.com.frotasPro.api.service.configuracaoempresa.ConfiguracaoEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/configuracao-empresa")
@RequiredArgsConstructor
public class ConfiguracaoEmpresaController {

    private final ConfiguracaoEmpresaService configuracaoEmpresaService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ConfiguracaoEmpresaResponse> buscar() {
        return ResponseEntity.ok(configuracaoEmpresaService.buscar());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PutMapping
    public ResponseEntity<ConfiguracaoEmpresaResponse> atualizar(@Valid @RequestBody ConfiguracaoEmpresaUpdateRequest request) {
        return ResponseEntity.ok(configuracaoEmpresaService.atualizar(request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConfiguracaoEmpresaResponse> atualizarLogo(@RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(configuracaoEmpresaService.atualizarLogo(arquivo));
    }
}
