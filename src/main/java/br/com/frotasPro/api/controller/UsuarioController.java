package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.UsuarioRequest;
import br.com.frotasPro.api.controller.request.UsuarioSenhaSelfRequest;
import br.com.frotasPro.api.controller.request.UsuarioSenhaUpdateRequest;
import br.com.frotasPro.api.controller.request.UsuarioUpdateRequest;
import br.com.frotasPro.api.controller.response.UsuarioResponse;
import br.com.frotasPro.api.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/usuario")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = "usuario_listar", allEntries = true),
            @CacheEvict(value = "usuario_buscar_id", allEntries = true)
    })
    public UsuarioResponse registar(@Valid @RequestBody UsuarioRequest request){
        return usuarioService.registar(request);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @GetMapping
    public Page<UsuarioResponse> listar(
            @RequestParam(value = "q", required = false)
            @Size(max = 150, message = "Filtro inválido")
            String q,
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
    @Caching(evict = {
            @CacheEvict(value = "usuario_listar", allEntries = true),
            @CacheEvict(value = "usuario_buscar_id", allEntries = true)
    })
    public UsuarioResponse atualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.atualizar(id, request);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\')")
    @PatchMapping("/{id}/ativo")
    @Caching(evict = {
            @CacheEvict(value = "usuario_listar", allEntries = true),
            @CacheEvict(value = "usuario_buscar_id", allEntries = true)
    })
    public UsuarioResponse atualizarAtivo(@PathVariable UUID id, @RequestParam("ativo") boolean ativo) {
        return usuarioService.atualizarAtivo(id, ativo);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\')")
    @PutMapping("/{id}/senha")
    @Caching(evict = {
            @CacheEvict(value = "usuario_listar", allEntries = true),
            @CacheEvict(value = "usuario_buscar_id", allEntries = true)
    })
    public ResponseEntity<Void> atualizarSenha(@PathVariable UUID id, @Valid @RequestBody UsuarioSenhaUpdateRequest request) {
        usuarioService.atualizarSenha(id, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/senha")
    @Caching(evict = {
            @CacheEvict(value = "usuario_listar", allEntries = true),
            @CacheEvict(value = "usuario_buscar_id", allEntries = true)
    })
    public ResponseEntity<Void> atualizarMinhaSenha(@Valid @RequestBody UsuarioSenhaSelfRequest request) {
        usuarioService.atualizarMinhaSenha(request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping("/motoristas")
    @Caching(evict = {
            @CacheEvict(value = "usuario_listar", allEntries = true),
            @CacheEvict(value = "usuario_buscar_id", allEntries = true)
    })
    public ResponseEntity<List<String>> criarUsuariosPelosMotoristas(
            @RequestParam("matriculas")
            @NotEmpty(message = "Matrículas são obrigatórias")
            List<
                    @NotBlank(message = "Matrícula inválida")
                    @Size(max = 50, message = "Matrícula inválida")
                            String
                    > codigo
    ) {
        List<String> resultado = usuarioService.criarUsuariosPelosMotoristas(codigo);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
