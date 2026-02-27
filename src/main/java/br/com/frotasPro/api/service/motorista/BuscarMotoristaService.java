package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MotoristaMapper;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarMotoristaService {

    private final MotoristaRepository motoristaRepository;

    @Transactional(readOnly = true)
    @Cacheable("motorista_buscar_codigo")
    public MotoristaResponse buscar(String codigo) {
        Motorista motorista = motoristaRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Motorista não encontrado: " + codigo));

        return MotoristaMapper.toResponse(motorista);
    }
}
