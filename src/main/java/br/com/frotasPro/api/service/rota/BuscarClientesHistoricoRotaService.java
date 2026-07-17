package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.controller.response.ClienteHistoricoRotaResponse;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.projections.ClienteHistoricoRotaProjection;
import br.com.frotasPro.api.repository.CargaNotaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Deriva, a partir do histórico de cargas já finalizadas, quais clientes
 * costumam aparecer em uma rota. Não grava nada novo — é sempre calculado
 * na hora a partir de tb_carga_nota, então não interfere no fluxo de carga.
 */
@Service
@RequiredArgsConstructor
public class BuscarClientesHistoricoRotaService {

    private final RotaRepository rotaRepository;
    private final CargaNotaRepository cargaNotaRepository;

    @Transactional(readOnly = true)
    public List<ClienteHistoricoRotaResponse> buscar(String codigoRota) {
        rotaRepository.findByCodigo(codigoRota.trim())
                .orElseThrow(() -> new ObjectNotFound("Rota não encontrada para o código: " + codigoRota));

        List<ClienteHistoricoRotaProjection> historico =
                cargaNotaRepository.buscarClientesHistoricoPorRota(codigoRota.trim());

        return historico.stream()
                .map(p -> ClienteHistoricoRotaResponse.builder()
                        .cliente(p.getCliente())
                        .quantidadeCargas(p.getQuantidadeCargas())
                        .ultimaCargaEm(p.getUltimaCargaEm())
                        .build())
                .toList();
    }
}
