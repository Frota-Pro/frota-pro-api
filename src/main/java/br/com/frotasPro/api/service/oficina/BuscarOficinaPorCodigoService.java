package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.OficinaMapper;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarOficinaPorCodigoService {

    private final OficinaRepository oficinaRepository;

    @Transactional(readOnly = true)
    public OficinaResponse porCodigo(String codigo) {
        Oficina oficina = oficinaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada para o código: " + codigo));
        return OficinaMapper.toResponse(oficina);
    }
}
