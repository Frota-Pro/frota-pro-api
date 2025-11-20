package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.OficinaRequest;
import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.service.oficina.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oficina")
public class OficinaController {

    private final CriarOficinaService criarService;
    private final BuscarOficinaPorIdService buscarPorIdService;
    private final BuscarTodasOficinasService buscarTodasService;
    private final AtualizarOficinaService atualizarService;
    private final DeletarOficinaService deletarService;

    @PostMapping
    public OficinaResponse criar(@RequestBody OficinaRequest request) {
        return criarService.criar(request);
    }

    @GetMapping("/{id}")
    public OficinaResponse buscarPorId(@PathVariable UUID id) {
        return buscarPorIdService.buscar(id);
    }

    @GetMapping
    public List<OficinaResponse> listar() {
        return buscarTodasService.listar();
    }

    @PutMapping("/{id}")
    public OficinaResponse atualizar(@PathVariable UUID id,
                                     @RequestBody OficinaRequest request) {
        return atualizarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
