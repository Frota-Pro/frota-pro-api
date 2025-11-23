package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.AbastecimentoRequest;
import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.service.abastecimento.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/abastecimento")
public class AbastecimentoController {

    private final CriarAbastecimentoService criarService;
    private final BuscarAbastecimentoPorCodigoService buscarAbastecimentoPorCodigoService;
    private final ListarAbastecimentosService listarService;
    private final AbastecimentoUpdateService atualizarService;
    private final DeletarAbastecimentoService deletarService;

    @PostMapping
    public AbastecimentoResponse criar(@RequestBody AbastecimentoRequest request) {
        return criarService.criar(request);
    }

    @GetMapping("/{id}")
    public AbastecimentoResponse buscarPorCodigo(@PathVariable String codigo) {
        return buscarAbastecimentoPorCodigoService.buscar(codigo);
    }

    @GetMapping
    public List<AbastecimentoResponse> listar() {
        return listarService.listar();
    }

    @PutMapping("/{id}")
    public AbastecimentoResponse atualizar(@PathVariable String codigo,
                                           @RequestBody AbastecimentoRequest request) {
        return atualizarService.atualizar(codigo, request);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        deletarService.deletar(id);
    }
}
