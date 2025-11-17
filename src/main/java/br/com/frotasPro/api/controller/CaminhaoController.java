package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.CaminhaoRequest;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.service.caminhao.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/caminhao")
@AllArgsConstructor
public class CaminhaoController {

    private final ListarCaminhaoService listarCaminhaoService;
    private final BuscarCaminhaoService buscarCaminhaoService;
    private final CriarCaminhaoService criarCaminhaoService;
    private final AtualizarCaminhaoService atualizarCaminhaoService;
    private final DeletarCaminhaoService deletarCaminhaoService;


    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\',)")
    @GetMapping
    public ResponseEntity<Page<CaminhaoResponse>> listar(Pageable pageable) {
        Page<CaminhaoResponse> caminhoes = listarCaminhaoService.listar(pageable);
        return ResponseEntity.ok(caminhoes);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\',)")
    @GetMapping("/{codigo}")
    public ResponseEntity<CaminhaoResponse> buscarPorCodigo(@PathVariable String codigo) {
        CaminhaoResponse caminhao = buscarCaminhaoService.porCodigo(codigo);
        return ResponseEntity.ok(caminhao);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping
    public ResponseEntity<CaminhaoResponse> registrar(
            @Valid @RequestBody CaminhaoRequest request) {

        CaminhaoResponse caminhao = criarCaminhaoService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(caminhao.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(caminhao);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PutMapping("/{codigo}")
    public ResponseEntity<CaminhaoResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody CaminhaoRequest request) {

        CaminhaoResponse caminhaoAtualizado = atualizarCaminhaoService.atualizar(codigo, request);
        return ResponseEntity.ok(caminhaoAtualizado);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarCaminhaoService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }
}
