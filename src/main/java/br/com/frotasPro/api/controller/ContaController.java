package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ContaRequest;
import br.com.frotasPro.api.controller.response.ContaResponse;
import br.com.frotasPro.api.service.conta.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conta")
public class ContaController {

    private final CriarContaService criarService;
    private final BuscarContaPorIdService buscarPorIdService;
    private final ListarContaService listarService;
    private final AtualizarContaService atualizarService;
    private final DeletarContaService deletarService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContaResponse criar(@Valid @RequestBody ContaRequest request) {
        return criarService.criar(request);
    }

    @GetMapping("/{id}")
    public ContaResponse buscarPorId(@PathVariable UUID id) {
        return buscarPorIdService.buscarPorId(id);
    }

    @GetMapping
    public List<ContaResponse> listar() {
        return listarService.listar();
    }

    @PutMapping("/{id}")
    public ContaResponse atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ContaRequest request
    ) {
        return atualizarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
