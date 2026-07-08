package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.request.CargaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.CargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Ajudante;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.logistica.domain.Rota;
import br.com.frotasPro.api.modules.logistica.mapper.CargaMapper;
import br.com.frotasPro.api.modules.logistica.repository.AjudanteRepository;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.modules.logistica.repository.RotaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtualizarCargaService {

    private final CargaRepository cargaRepository;
    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final RotaRepository rotaRepository;
    private final AjudanteRepository ajudanteRepository;

    @Transactional
    public CargaResponse atualizar(String numeroCarga, CargaRequest request) {

        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada para o número: " + numeroCarga));

        Motorista motorista = motoristaRepository.findByCodigoAndAtivoTrue(request.getCodigoMotorista().trim())
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado."));

        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(request.getCodigoCaminhao().trim())
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado."));

        Rota rota = rotaRepository.findByCodigo(request.getCodigoRota().trim())
                .orElseThrow(() -> new ObjectNotFound("Rota não encontrada."));

        List<Ajudante> ajudantes = Collections.emptyList();
        if (request.getCodigosAjudantes() != null && !request.getCodigosAjudantes().isEmpty()) {
            ajudantes = request.getCodigosAjudantes().stream()
                    .map(String::trim)
                    .map(cod -> ajudanteRepository.findByCodigoAndAtivoTrue(cod)
                            .orElseThrow(() -> new ObjectNotFound("Ajudante não encontrado: " + cod)))
                    .toList();
        }

        CargaMapper.updateEntity(carga, request, motorista, caminhao, rota, ajudantes);

        carga = cargaRepository.save(carga);

        return CargaMapper.toResponse(carga);
    }
}
