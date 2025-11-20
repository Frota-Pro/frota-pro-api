package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.MecanicoRequest;
import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.service.mecanico.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mecanico")
public class MecanicoController {

    private final CriarMecanicoService criarService;
    private final BuscarMecanicoPorIdService buscarPorIdService;
    private final BuscarTodosMecanicosService buscarTodosService;
    private final AtualizarMecanicoService atualizarService;
    private final DeletarMecanicoService deletarService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MecanicoResponse criar(@RequestBody MecanicoRequest request) {
        return criarService.criar(request);
    }

    @GetMapping
    public List<MecanicoResponse> listar() {
        return buscarTodosService.listar();
    }

    @GetMapping("/{id}")
    public MecanicoResponse buscarPorId(@PathVariable UUID id) {
        return buscarPorIdService.buscar(id);
    }

    @PutMapping("/{id}")
    public MecanicoResponse atualizar(@PathVariable UUID id, @RequestBody MecanicoRequest request) {
        return atualizarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
