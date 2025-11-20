package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.repository.PneuRepository;
import br.com.frotasPro.api.validator.ValidaSePneuExiste;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarPneuService {

    private final PneuRepository pneuRepository;
    private final ValidaSePneuExiste validaSePneuExiste;

    public void deletar(UUID id) {
        validaSePneuExiste.validar(id);
        pneuRepository.deleteById(id);
    }
}

