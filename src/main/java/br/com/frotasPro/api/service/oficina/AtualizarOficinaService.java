package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.controller.request.OficinaRequest;
import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.mapper.OficinaMapper;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.OficinaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class AtualizarOficinaService {

    private final OficinaRepository repository;

    public OficinaResponse atualizar(UUID id, OficinaRequest request) {

        Oficina oficina = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oficina não encontrada!"));

        oficina.setNome(request.getNome());
        oficina.setCodigo(request.getCodigo());

        repository.save(oficina);

        return toResponse(oficina);
    }
}
