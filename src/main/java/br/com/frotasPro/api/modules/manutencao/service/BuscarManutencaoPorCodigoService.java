package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.response.ManutencaoResponse;
import br.com.frotasPro.api.modules.manutencao.domain.Manutencao;
import br.com.frotasPro.api.modules.manutencao.repository.ManutencaoRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.modules.manutencao.mapper.ManutencaoMapper.toResponse;

@Service
@AllArgsConstructor
public class BuscarManutencaoPorCodigoService {

    private final ManutencaoRepository manutencaoRepository;

    @Transactional(readOnly = true)
    @Cacheable("manutencao_buscar_codigo")
    public ManutencaoResponse buscar(String codigo) {
        Manutencao manutencao = manutencaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Manutenção não encontrada"));
        return toResponse(manutencao);
    }
}
