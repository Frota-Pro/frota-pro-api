package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MetaRequest;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.service.meta.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meta")
public class MetaController {

    private final CriarMetaService criarService;
    private final BuscarMetaPorIdService buscarPorIdService;
    private final BuscarTodasMetasService buscarTodasService;
    private final AtualizarMetaService atualizarService;
    private final DeletarMetaService deletarService;

    @PostMapping
    public MetaResponse criar(@RequestBody MetaRequest request) {
        return criarService.criar(request);
    }

    @GetMapping("/{id}")
    public MetaResponse buscarPorId(@PathVariable UUID id) {
        return buscarPorIdService.buscar(id);
    }

    @GetMapping
    public List<MetaResponse> listar() {
        return buscarTodasService.listar();
    }

    @PutMapping("/{id}")
    public MetaResponse atualizar(@PathVariable UUID id,
                                  @RequestBody MetaRequest request) {
        return atualizarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
