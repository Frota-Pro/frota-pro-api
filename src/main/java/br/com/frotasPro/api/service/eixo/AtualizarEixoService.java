package br.com.frotasPro.api.service.eixo;


import br.com.frotasPro.api.controller.request.EixoRequest;
import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.EixoRepository;
import br.com.frotasPro.api.validator.ValidaSeCaminhaoExiste;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.EixoMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class AtualizarEixoService {

    private final EixoRepository eixoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final ValidaSeCaminhaoExiste validaSeCaminhaoExiste;

    public EixoResponse atualizar(UUID id, EixoRequest request) {

        Eixo eixo = eixoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Eixo não encontrado"));

        eixo.setNumero(request.getNumero());

        if (request.getCaminhaoId() != null) {

            validaSeCaminhaoExiste.validar(request.getCaminhaoId());

            Caminhao caminhao = caminhaoRepository.findById(request.getCaminhaoId()).get();

            eixo.setCaminhao(caminhao);
        }

        eixoRepository.save(eixo);

        return toResponse(eixo);
    }
}
