package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.CidadeResumoResponse;
import br.com.frotasPro.api.controller.response.ClienteHistoricoRotaResponse;
import br.com.frotasPro.api.service.cidade.BuscarClientesPorCidadeService;
import br.com.frotasPro.api.service.cidade.ListarCidadesService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cidades atendidas pelo sistema — derivadas do histórico de cargas
 * (tb_carga_nota), não é um cadastro próprio. Complementa /rota: a rota
 * descreve o trajeto planejado (dias, cidades do percurso), a cidade
 * agrupa os clientes que realmente existem ali, independente de qual
 * rota os atendeu ao longo do tempo.
 */
@RestController
@RequestMapping("/cidades")
@AllArgsConstructor
public class CidadeController {

    private final ListarCidadesService listarCidadesService;
    private final BuscarClientesPorCidadeService buscarClientesPorCidadeService;

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<List<CidadeResumoResponse>> listar() {
        return ResponseEntity.ok(listarCidadesService.listar());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/{cidade}/clientes")
    public ResponseEntity<List<ClienteHistoricoRotaResponse>> clientes(@PathVariable String cidade) {
        return ResponseEntity.ok(buscarClientesPorCidadeService.buscar(cidade));
    }
}
