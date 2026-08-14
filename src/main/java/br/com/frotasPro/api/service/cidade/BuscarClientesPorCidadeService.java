package br.com.frotasPro.api.service.cidade;

import br.com.frotasPro.api.controller.response.ClienteHistoricoRotaResponse;
import br.com.frotasPro.api.projections.ClienteHistoricoRotaProjection;
import br.com.frotasPro.api.repository.CargaNotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Clientes de uma cidade, derivados do histórico de notas fiscais —
 * mesma lógica de BuscarClientesHistoricoRotaService, só que agrupando
 * por cidade em vez de por rota (cliente é da cidade dele, não da rota).
 */
@Service
@RequiredArgsConstructor
public class BuscarClientesPorCidadeService {

    private final CargaNotaRepository cargaNotaRepository;

    @Transactional(readOnly = true)
    public List<ClienteHistoricoRotaResponse> buscar(String cidade) {
        List<ClienteHistoricoRotaProjection> historico =
                cargaNotaRepository.buscarClientesPorCidade(cidade.trim());

        return historico.stream()
                .map(p -> ClienteHistoricoRotaResponse.builder()
                        .cliente(p.getCliente())
                        .cidade(p.getCidade())
                        .quantidadeCargas(p.getQuantidadeCargas())
                        .ultimaCargaEm(p.getUltimaCargaEm())
                        .build())
                .toList();
    }
}
