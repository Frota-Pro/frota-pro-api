package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.response.OficinaResponse;
import br.com.frotasPro.api.modules.manutencao.domain.Oficina;
import br.com.frotasPro.api.modules.manutencao.mapper.OficinaMapper;
import br.com.frotasPro.api.modules.manutencao.repository.OficinaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarOficinaPorCodigoService {

    private final OficinaRepository oficinaRepository;

    @Transactional(readOnly = true)
    @Cacheable("oficina_buscar_codigo")
    public OficinaResponse porCodigo(String codigo) {
        Oficina oficina = oficinaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada para o código: " + codigo));
        return OficinaMapper.toResponse(oficina);
    }
}
