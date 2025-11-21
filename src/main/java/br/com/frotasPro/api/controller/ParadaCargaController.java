package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.service.paradaCarga.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parada-carga")
public class ParadaCargaController {

    private final CriarParadaCargaService criarService;
    private final BuscarParadaCargaPorIdService buscarPorIdService;
    private final ListarParadaCargaService buscarTodasService;
    private final AtualizarParadaCargaService atualizarService;
    private final DeletarParadaCargaService deletarService;

    @PostMapping
    public ParadaCargaResponse criar(@RequestBody ParadaCargaRequest request) {
        return criarService.criar(request);
    }

    @GetMapping("/{id}")
    public ParadaCargaResponse buscarPorId(@PathVariable UUID id) {
        return buscarPorIdService.buscar(id);
    }

    @GetMapping
    public List<ParadaCargaResponse> listar() {
        return buscarTodasService.listar();
    }

    @PutMapping("/{id}")
    public ParadaCargaResponse atualizar(@PathVariable UUID id,
                                         @RequestBody ParadaCargaRequest request) {
        return atualizarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
