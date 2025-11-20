package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.controller.request.EixoRequest;
import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.mapper.EixoMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.EixoRepository;
import br.com.frotasPro.api.validator.ValidaSeCaminhaoExiste;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static br.com.frotasPro.api.mapper.EixoMapper.toResponse;

@Service
@AllArgsConstructor
public class CriarEixoService {

    private final EixoRepository eixoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final ValidaSeCaminhaoExiste validaSeCaminhaoExiste;

    public EixoResponse criar(EixoRequest request) {

        validaSeCaminhaoExiste.validar(request.getCaminhaoId());

        Caminhao caminhao = caminhaoRepository.findById(request.getCaminhaoId()).get();

        Eixo eixo = Eixo.builder()
                .numero(request.getNumero())
                .caminhao(caminhao)
                .build();

        eixoRepository.save(eixo);

        return toResponse(eixo);
    }
}
