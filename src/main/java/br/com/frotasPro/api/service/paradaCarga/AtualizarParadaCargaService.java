package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.TipoParada;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.ParadaCargaMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AtualizarParadaCargaService {

    private final ParadaCargaRepository repository;
    private final CargaRepository cargaRepository;

    public ParadaCargaResponse atualizar(UUID id, ParadaCargaRequest request) {

        ParadaCarga entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parada não encontrada"));

        Carga carga = cargaRepository.findById(request.getCargaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Carga não encontrada"));

        entity.setCarga(carga);
        entity.setTipoParada(TipoParada.valueOf(request.getTipoParada()));
        entity.setDtInicio(request.getDtInicio());
        entity.setDtFim(request.getDtFim());
        entity.setCidade(request.getCidade());
        entity.setLocal(request.getLocal());
        entity.setKmOdometro(request.getKmOdometro());
        entity.setObservacao(request.getObservacao());

        repository.save(entity);

        return toResponse(entity);
    }
}
