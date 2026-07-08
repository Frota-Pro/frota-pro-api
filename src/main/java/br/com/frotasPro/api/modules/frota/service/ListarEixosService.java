package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.response.EixoResponse;
import br.com.frotasPro.api.modules.frota.mapper.EixoMapper;
import br.com.frotasPro.api.modules.frota.repository.EixoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ListarEixosService {

    private final EixoRepository eixoRepository;

    public Page<EixoResponse> listar(Pageable pageable) {
        return eixoRepository.findAll(pageable)
                .map(EixoMapper::toResponse);
    }
}

