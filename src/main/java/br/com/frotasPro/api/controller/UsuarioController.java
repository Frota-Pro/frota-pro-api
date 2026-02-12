package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.UsuarioRequest;
import br.com.frotasPro.api.controller.request.UsuarioSenhaSelfRequest;
import br.com.frotasPro.api.controller.request.UsuarioSenhaUpdateRequest;
import br.com.frotasPro.api.controller.request.UsuarioUpdateRequest;
import br.com.frotasPro.api.controller.response.UsuarioResponse;
import br.com.frotasPro.api.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuario")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping
    public UsuarioResponse registar(@Valid @RequestBody UsuarioRequest request){
        return usuarioService.registar(request);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @GetMapping
    public Page<UsuarioResponse> listar(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "ativo", required = false) Boolean ativo,
            Pageable pageable
    ) {
        return usuarioService.listar(q, ativo, pageable);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\')")
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.atualizar(id, request);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\')")
    @PatchMapping("/{id}/ativo")
    public UsuarioResponse atualizarAtivo(@PathVariable UUID id, @RequestParam("ativo") boolean ativo) {
        return usuarioService.atualizarAtivo(id, ativo);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\')")
    @PutMapping("/{id}/senha")
    public ResponseEntity<Void> atualizarSenha(@PathVariable UUID id, @Valid @RequestBody UsuarioSenhaUpdateRequest request) {
        usuarioService.atualizarSenha(id, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/senha")
    public ResponseEntity<Void> atualizarMinhaSenha(@Valid @RequestBody UsuarioSenhaSelfRequest request) {
        usuarioService.atualizarMinhaSenha(request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping("/motoristas")
    public ResponseEntity<List<String>> criarUsuariosPelosMotoristas(@RequestParam("matriculas") List<String> codigo) {
        List<String> resultado = usuarioService.criarUsuariosPelosMotoristas(codigo);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
