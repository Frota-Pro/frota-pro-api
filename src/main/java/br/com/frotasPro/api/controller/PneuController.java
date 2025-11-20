package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.service.pneu.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@AllArgsConstructor
@RequestMapping("/pneu")
public class PneuController {

    private final CriarPneuService criarPneuService;
    private final ListarPneusService listarPneusService;
    private final BuscarPneuPorIdService buscarPneuPorIdService;
    private final AtualizarPneuService atualizarPneuService;
    private final DeletarPneuService deletarPneuService;

    @PostMapping
    public PneuResponse criarPneu(@Valid @RequestBody PneuRequest request) {
        return criarPneuService.criar(request);
    }

    @GetMapping
    public List<PneuResponse> listarPneus() {
        return listarPneusService.listar();
    }

    @GetMapping("/{id}")
    public PneuResponse buscar(@PathVariable UUID id) {
        return buscarPneuPorIdService.buscar(id);
    }

    @PutMapping("/{id}")
    public PneuResponse atualizar(
            @PathVariable UUID id,
            @RequestBody PneuRequest request
    ) {
        return atualizarPneuService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarPneuService.deletar(id);
    }
}
