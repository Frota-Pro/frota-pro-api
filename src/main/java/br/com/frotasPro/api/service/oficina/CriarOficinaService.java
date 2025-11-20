package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.controller.request.OficinaRequest;
import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.frotasPro.api.mapper.OficinaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class CriarOficinaService {

    private final OficinaRepository repository;

    public OficinaResponse criar(OficinaRequest request) {

        Oficina oficina = Oficina.builder()
                .nome(request.getNome())
                .codigo(request.getCodigo())
                .build();

        repository.save(oficina);

        return toResponse(oficina);
    }
}
