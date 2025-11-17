package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.request.CargaRequest;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.*;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CriarCargaService {

    private final CargaRepository cargaRepository;
    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final RotaRepository rotaRepository;
    private final AjudanteRepository ajudanteRepository;

    @Transactional
    public CargaResponse criar(CargaRequest request) {

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

        Carga carga = CargaMapper.toEntity(request, motorista, caminhao, rota, ajudantes);

        carga = cargaRepository.save(carga);

        return CargaMapper.toResponse(carga);
    }
}
