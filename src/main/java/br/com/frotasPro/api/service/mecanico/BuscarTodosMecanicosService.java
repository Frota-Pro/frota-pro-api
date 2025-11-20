package br.com.frotasPro.api.service.mecanico;

import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.mapper.MecanicoMapper;
import br.com.frotasPro.api.repository.MecanicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarTodosMecanicosService {

    private final MecanicoRepository mecanicoRepository;

    public List<MecanicoResponse> listar() {
        return mecanicoRepository.findAll()
                .stream()
                .map(MecanicoMapper::toResponse)
                .toList();
    }
}
