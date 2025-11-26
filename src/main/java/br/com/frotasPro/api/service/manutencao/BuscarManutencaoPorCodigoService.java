package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;

@Service
@AllArgsConstructor
public class BuscarManutencaoPorCodigoService {

    private final ManutencaoRepository manutencaoRepository;

    public ManutencaoResponse buscar(String codigo) {
        Manutencao manutencao = manutencaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Manutenção não encontrada"));
        return toResponse(manutencao);
    }
}
