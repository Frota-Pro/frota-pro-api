package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.DespesaParadaRequest;
import br.com.frotasPro.api.controller.response.DespesaParadaResponse;
import br.com.frotasPro.api.service.despesaParada.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/despesas-parada")
public class DespesaParadaController {

    private final CriarDespesaParadaService criarService;
    private final DespesaParadaBuscarPorIdService buscarPorIdService;
    private final BuscarTodosDespesaParadaService buscarTodosService;
    private final AtualizarDespesaParadaService atualizarService;
    private final DespesaParadaDeleteService deleteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DespesaParadaResponse criar(@RequestBody @Valid DespesaParadaRequest request) {
        return criarService.criar(request);
    }

    @GetMapping("/{id}")
    public DespesaParadaResponse buscarPorId(@PathVariable UUID id) {
        return buscarPorIdService.buscar(id);
    }

    @GetMapping
    public List<DespesaParadaResponse> listar() {
        return buscarTodosService.listar();
    }

    @PutMapping("/{id}")
    public DespesaParadaResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid DespesaParadaRequest request
    ) {
        return atualizarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deleteService.deletar(id);
    }
}
