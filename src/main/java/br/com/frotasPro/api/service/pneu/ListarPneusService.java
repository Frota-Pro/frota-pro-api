package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.mapper.PneuMapper;
import br.com.frotasPro.api.repository.PneuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ListarPneusService {

    private final PneuRepository pneuRepository;

    public List<PneuResponse> listar() {
        return pneuRepository.findAll()
                .stream()
                .map(PneuMapper::toResponse)
                .toList();
    }
}
