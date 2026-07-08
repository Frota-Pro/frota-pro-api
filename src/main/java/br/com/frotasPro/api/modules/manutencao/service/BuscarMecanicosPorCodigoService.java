package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.response.MecanicoResponse;
import br.com.frotasPro.api.modules.manutencao.domain.Mecanico;
import br.com.frotasPro.api.modules.manutencao.mapper.MecanicoMapper;
import br.com.frotasPro.api.modules.manutencao.repository.MecanicoRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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
