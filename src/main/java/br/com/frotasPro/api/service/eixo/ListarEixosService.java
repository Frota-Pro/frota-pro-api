package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.mapper.EixoMapper;
import br.com.frotasPro.api.repository.EixoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ListarEixosService {

    private final EixoRepository eixoRepository;

    public List<EixoResponse> listar() {

        return eixoRepository.findAll()
                .stream()
                .map(EixoMapper::toResponse)
                .toList();
    }
}

