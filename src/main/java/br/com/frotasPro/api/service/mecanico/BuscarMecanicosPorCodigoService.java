package br.com.frotasPro.api.service.mecanico;

import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.domain.Mecanico;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MecanicoMapper;
import br.com.frotasPro.api.repository.MecanicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarMecanicosPorCodigoService {

    private final MecanicoRepository mecanicoRepository;

    @Transactional(readOnly = true)
    public MecanicoResponse BuscarPorCodigo(String codigo) {
        Mecanico mecanico = mecanicoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Mecanico não encontrada para o código: "+ codigo));
        return MecanicoMapper.toResponse(mecanico);

    }
}
