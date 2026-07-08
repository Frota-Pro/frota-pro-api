package br.com.frotasPro.api.modules.financeiro.controller;

import br.com.frotasPro.api.modules.financeiro.dto.request.GrupoContaRequest;
import br.com.frotasPro.api.modules.financeiro.dto.response.GrupoContaResponse;
import br.com.frotasPro.api.modules.financeiro.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/grupos-conta")
public class GrupoContaController {

    private final CriarGrupoContaService criarService;
    private final ListarGrupoContaService listarService;
    private final BuscarGrupoContaPorIdService buscarPorIdService;
    private final BuscarGrupoContaPorCodigoService buscarGrupoContaPorCodigoService;
    private final AtualizarGrupoContaService atualizarService;
    private final DeletarGrupoContaService deletarService;

    @PostMapping
    public GrupoContaResponse criar(@Valid @RequestBody GrupoContaRequest request) {
        return criarService.criar(request);
    }

    @GetMapping
    public List<GrupoContaResponse> listar() {
        return listarService.listar();
    }

    @GetMapping("/{id}")
    public GrupoContaResponse buscarPorId(@PathVariable UUID id) {
        return buscarPorIdService.buscarPorId(id);
    }

    @GetMapping("/codigo")
    public ResponseEntity<GrupoContaResponse> buscarGrupoContaPorCodigo(@RequestParam("codigo") String codigo){
        GrupoContaResponse grupoConta = buscarGrupoContaPorCodigoService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(grupoConta);
    }

    @PutMapping("/{id}")
    public GrupoContaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody GrupoContaRequest request) {
        return atualizarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
