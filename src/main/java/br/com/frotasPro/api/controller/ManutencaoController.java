package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.service.manutencao.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manutencoes")
public class ManutencaoController {

    private final CriarManutencaoService criarManutencaoService;
    private final BuscarManutencaoPorIdService buscarManutencaoPorIdService;
    private final BuscarTodasManutencoesService buscarTodasManutencoesService;
    private final BuscarManutencaoPorCodigoService buscarManutencaoPorCodigoService;
    private final AtualizarManutencaoService atualizarManutencaoService;
    private final DeletarManutencaoService deletarManutencaoService;

    @PostMapping
    public ManutencaoResponse criar(@RequestBody ManutencaoRequest request) {
        return criarManutencaoService.criar(request);
    }

    @GetMapping("/{id}")
    public ManutencaoResponse buscarPorId(@PathVariable UUID id) {
        return buscarManutencaoPorIdService.buscar(id);
    }

    @GetMapping
    public List<ManutencaoResponse> listarTodos() {
        return buscarTodasManutencoesService.buscarTodos();
    }

    @GetMapping("/codigo")
    public ResponseEntity<ManutencaoResponse> buscarManutencaoPorCodigo(@RequestParam("codigo") String codigo){
        ManutencaoResponse manutencao = buscarManutencaoPorCodigoService.buscarManutencaoCodigo(codigo);
        return ResponseEntity.ok(manutencao);
    }

    @PutMapping("/{id}")
    public ManutencaoResponse atualizar(@PathVariable UUID id,
                                        @RequestBody ManutencaoRequest request) {
        return atualizarManutencaoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        deletarManutencaoService.deletar(id);
    }
}
