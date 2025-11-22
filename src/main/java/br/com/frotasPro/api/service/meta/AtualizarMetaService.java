package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.request.MetaRequest;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizarMetaService {

    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;

    public MetaResponse atualizar(UUID id, MetaRequest request) {

        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meta não encontrada"));

        Caminhao caminhao = caminhaoRepository.findById(request.getCaminhaoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caminhão não encontrado"));

        Motorista motorista = motoristaRepository.findById(request.getMotoristaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motorista não encontrado"));

        meta.setDataIncio(request.getDataIncio());
        meta.setDataFim(request.getDataFim());
        meta.setTipoMeta(request.getTipoMeta());
        meta.setValorMeta(request.getValorMeta());
        meta.setValorRealizado(request.getValorRealizado());
        meta.setUnidade(request.getUnidade());
        meta.setStatusMeta(request.getStatusMeta());
        meta.setDescricao(request.getDescricao());
        meta.setCaminhao(caminhao);
        meta.setMotorista(motorista);

        metaRepository.save(meta);

        return MetaMapper.toResponse(meta);
    }
}

