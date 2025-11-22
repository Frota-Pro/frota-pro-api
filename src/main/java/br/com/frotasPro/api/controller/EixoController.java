package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.EixoRequest;
import br.com.frotasPro.api.controller.response.ContaResponse;
import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.service.eixo.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@AllArgsConstructor
@RequestMapping("/eixo")
public class EixoController {

    private final CriarEixoService criarEixoService;
    private final ListarEixosService listarEixosService;
    private final BuscarEixoPorIdService buscarEixoPorIdService;
    private final AtualizarEixoService atualizarEixoService;
    private final DeletarEixoService deletarEixoService;

    @PostMapping
    public EixoResponse criarEixo(@Valid @RequestBody EixoRequest request) {
        return criarEixoService.criar(request);
    }

    @GetMapping
    public List<EixoResponse> listarEixo() {
        return listarEixosService.listar();
    }

    @GetMapping("/{id}")
    public EixoResponse buscar(@PathVariable UUID id) {
        return buscarEixoPorIdService.buscar(id);
    }

    @PutMapping("/{id}")
    public EixoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody EixoRequest request
    ) {
        return atualizarEixoService.atualizar(id, request);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarEixoService.deletar(id);
    }
}
