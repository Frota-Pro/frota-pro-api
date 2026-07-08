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

@Service
@RequiredArgsConstructor
public class CriarEixoService {

    private final EixoRepository eixoRepository;
    private final CaminhaoRepository caminhaoRepository;

    @Transactional
    public EixoResponse criar(EixoRequest request) {

        Caminhao caminhao = caminhaoRepository.findByCodigo(request.getCodigoCaminhao())
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o código: " + request.getCodigoCaminhao()));

        Eixo eixo = Eixo.builder()
                .numero(request.getNumero())
                .caminhao(caminhao)
                .build();

        eixoRepository.save(eixo);

        return EixoMapper.toResponse(eixo);
    }
}
