package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.controller.request.EixoRequest;
import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.EixoMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.EixoRepository;
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
