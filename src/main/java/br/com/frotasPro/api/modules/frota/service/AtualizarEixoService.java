package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.request.EixoRequest;
import br.com.frotasPro.api.modules.frota.dto.response.EixoResponse;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.frota.domain.Eixo;
import br.com.frotasPro.api.modules.frota.mapper.EixoMapper;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.frota.repository.EixoRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizarEixoService {

    private final EixoRepository eixoRepository;
    private final CaminhaoRepository caminhaoRepository;

    @Transactional
    public EixoResponse atualizar(UUID id, EixoRequest request) {

        Eixo eixo = eixoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Eixo não encontrado para o id: " + id));

        if (request.getCodigoCaminhao() != null &&
                !request.getCodigoCaminhao().equals(eixo.getCaminhao().getCodigo())) {

            Caminhao caminhao = caminhaoRepository.findByCodigo(request.getCodigoCaminhao())
                    .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o código: " + request.getCodigoCaminhao()));

            eixo.setCaminhao(caminhao);
        }

        eixo.setNumero(request.getNumero());

        eixoRepository.save(eixo);

        return EixoMapper.toResponse(eixo);
    }
}
