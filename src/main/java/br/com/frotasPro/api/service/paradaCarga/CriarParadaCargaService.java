package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.mapper.ParadaCargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static br.com.frotasPro.api.mapper.ParadaCargaMapper.toEntity;
import static br.com.frotasPro.api.mapper.ParadaCargaMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CriarParadaCargaService {

    private final ParadaCargaRepository repository;
    private final CargaRepository cargaRepository;

    public ParadaCargaResponse criar(ParadaCargaRequest request) {

        Carga carga = cargaRepository.findById(request.getCargaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Carga não encontrada"));

        ParadaCarga entity = toEntity(request, carga);

        repository.save(entity);

        return toResponse(entity);
    }
}
