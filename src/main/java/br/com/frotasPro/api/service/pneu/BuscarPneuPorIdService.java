package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.repository.PneuRepository;
import br.com.frotasPro.api.validator.ValidaSePneuExiste;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.PneuMapper.toResponse;

@Service
@RequiredArgsConstructor
public class BuscarPneuPorIdService {

    private final PneuRepository pneuRepository;
    private final ValidaSePneuExiste validaSePneuExiste;

    public PneuResponse buscar(UUID id) {

        validaSePneuExiste.validar(id);

        var pneu = pneuRepository.findById(id).get();

        return toResponse(pneu);
    }
}
