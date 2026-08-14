package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.request.TransferirMotoristaCargaRequest;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Corrige o motorista de uma carga quando a carga foi faturada pra um
 * motorista no WinThor mas outro foi quem realmente saiu com ela (troca
 * de última hora, sem que o MDF-e/minuta sejam reemitidos). Marca a carga
 * como "definida manualmente" pra que o próximo sync do WinThor não
 * desfaça a correção.
 */
@Service
@RequiredArgsConstructor
public class TransferirMotoristaCargaService {

    private final CargaRepository cargaRepository;
    private final MotoristaRepository motoristaRepository;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    @Transactional
    public CargaResponse transferir(String numeroCarga, TransferirMotoristaCargaRequest request) {
        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCarga));

        String codigoMotorista = request.getCodigoMotorista().trim();
        Motorista motorista = motoristaRepository.findByCodigo(codigoMotorista)
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado: " + codigoMotorista));

        carga.setMotorista(motorista);
        carga.setMotoristaDefinidoManualmente(true);

        Carga cargaSalva = cargaRepository.save(carga);

        CargaResponse response = CargaMapper.toResponse(cargaSalva);
        CargaMapper.aplicarNumeroExibicao(response, cargaSalva, integracaoWinThorConfigService.isCargaIntegracaoAtiva());
        return response;
    }
}
