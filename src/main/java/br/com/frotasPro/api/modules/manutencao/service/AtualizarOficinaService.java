package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.request.OficinaRequest;
import br.com.frotasPro.api.modules.manutencao.dto.response.OficinaResponse;
import br.com.frotasPro.api.modules.manutencao.domain.Oficina;
import br.com.frotasPro.api.modules.manutencao.mapper.OficinaMapper;
import br.com.frotasPro.api.modules.manutencao.repository.OficinaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarOficinaService {

    private final OficinaRepository oficinaRepository;

    @Transactional
    public OficinaResponse atualizar(String codigo, OficinaRequest request) {

        Oficina oficina = oficinaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada para o id: " + codigo));

        oficina.setNome(request.getNome());

        oficina = oficinaRepository.save(oficina);

        return OficinaMapper.toResponse(oficina);
    }
}
