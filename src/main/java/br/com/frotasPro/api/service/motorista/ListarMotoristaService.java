package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.mapper.MotoristaMapper;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarMotoristaService {

    private final MotoristaRepository motoristaRepository;

    @Transactional(readOnly = true)
    public Page<MotoristaResponse> listar(Boolean ativo, String q, Pageable pageable) {
        return motoristaRepository.search(ativo, q, pageable).map(MotoristaMapper::toResponse);
    }
}
