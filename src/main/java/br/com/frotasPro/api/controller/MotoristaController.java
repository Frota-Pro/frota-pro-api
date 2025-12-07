package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MotoristaRequest;
import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.controller.response.RelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.service.motorista.*;
import br.com.frotasPro.api.service.relatorios.RelatorioMetaMensalMotoristaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/motorista")
@AllArgsConstructor
public class MotoristaController {

    private final ListarMotoristaService listarMotoristaService;
    private final BuscarMotoristaService buscarMotoristaService;
    private final CriarMotoristaService criarMotoristaService;
    private final AtualizarMotoristaService atualizarMotoristaService;
    private final DeletarMotoristaService deletarMotoristaService;
    private final RelatorioMetaMensalMotoristaService relatorioMetaMensalMotoristaService;


    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\')")
    @GetMapping("/{codigo}")
    public ResponseEntity<MotoristaResponse> buscarPorCodigo(@PathVariable String codigo) {
        MotoristaResponse motorista = buscarMotoristaService.buscar(codigo);
        return ResponseEntity.ok(motorista);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_CONSULTA\',)")
    @GetMapping
    public ResponseEntity<Page<MotoristaResponse>> listar(Pageable pageable) {
        Page<MotoristaResponse> motoristas = listarMotoristaService.listar(pageable);
        return ResponseEntity.ok(motoristas);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PostMapping
    public ResponseEntity<MotoristaResponse> registar(@Valid @RequestBody MotoristaRequest request){

        MotoristaResponse motorista = criarMotoristaService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(motorista.getCodigo())
                .toUri();
        return ResponseEntity.created(location).body(motorista);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @PutMapping("/{codigo}")
    public ResponseEntity<MotoristaResponse> atualizar(@PathVariable String codigo, @Valid @RequestBody MotoristaRequest request) {

        MotoristaResponse motoristaAtualizado =
                atualizarMotoristaService.atualizar(codigo, request);

        return ResponseEntity.ok(motoristaAtualizado);
    }

    @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_GERENTE_LOGISTICA\', \'ROLE_OPERADOR_LOGISTICA\')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarMotoristaService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{codigoMotorista}/meta-mensal")
    public ResponseEntity<RelatorioMetaMensalMotoristaResponse> gerar(
            @PathVariable String codigoMotorista,
            @RequestParam("inicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        RelatorioMetaMensalMotoristaResponse response =
                relatorioMetaMensalMotoristaService.gerar(codigoMotorista, inicio, fim);

        return ResponseEntity.ok(response);
    }
}
