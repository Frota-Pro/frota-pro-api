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

import static br.com.frotasPro.api.mapper.MetaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class CriarMetaService {

    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;

    public MetaResponse criar(MetaRequest request) {

        Caminhao caminhao = caminhaoRepository.findById(request.getCaminhaoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caminhão não encontrado"));

        Motorista motorista = motoristaRepository.findById(request.getMotoristaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motorista não encontrado"));

        Meta meta = Meta.builder()
                .dataIncio(request.getDataIncio())
                .dataFim(request.getDataFim())
                .tipoMeta(request.getTipoMeta())
                .valorMeta(request.getValorMeta())
                .valorRealizado(request.getValorRealizado())
                .unidade(request.getUnidade())
                .statusMeta(request.getStatusMeta())
                .descricao(request.getDescricao())
                .caminhao(caminhao)
                .motorista(motorista)
                .build();

        metaRepository.save(meta);
        return toResponse(meta);
    }
}
