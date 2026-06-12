package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.response.MecanicoResponse;
import br.com.frotasPro.api.modules.manutencao.mapper.MecanicoMapper;
import br.com.frotasPro.api.modules.manutencao.repository.MecanicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarTodosMecanicosService {

    private final MecanicoRepository mecanicoRepository;

    @Transactional(readOnly = true)
    public Page<MecanicoResponse> listar(Pageable pageable) {
        return mecanicoRepository.findAll(pageable)
                .map(MecanicoMapper::toResponse);
    }
}
