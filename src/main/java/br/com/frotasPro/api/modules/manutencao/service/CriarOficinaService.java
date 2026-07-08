package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.request.OficinaRequest;
import br.com.frotasPro.api.modules.manutencao.dto.response.OficinaResponse;
import br.com.frotasPro.api.modules.manutencao.domain.Oficina;
import br.com.frotasPro.api.modules.manutencao.mapper.OficinaMapper;
import br.com.frotasPro.api.modules.manutencao.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.modules.manutencao.mapper.OficinaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class CriarOficinaService {

    private final OficinaRepository oficinaRepository;

    @Transactional
    public OficinaResponse criar(OficinaRequest request) {

        Oficina oficina = Oficina.builder()
                .nome(request.getNome())
                .build();

        oficina = oficinaRepository.save(oficina);

        return OficinaMapper.toResponse(oficina);
    }
}
