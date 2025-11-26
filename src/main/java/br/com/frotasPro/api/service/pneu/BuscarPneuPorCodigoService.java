package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.domain.Pneu;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.PneuMapper;
import br.com.frotasPro.api.repository.PneuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarPneuPorCodigoService {

    private final PneuRepository pneuRepository;

    public PneuResponse buscar(String codigo) {
        Pneu pneu = pneuRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Pneu não encontrado para o código: " + codigo));
        return PneuMapper.toResponse(pneu);
    }
}
