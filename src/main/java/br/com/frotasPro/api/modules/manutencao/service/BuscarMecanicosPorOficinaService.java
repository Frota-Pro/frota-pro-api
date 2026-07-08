package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.response.MecanicoResponse;
import br.com.frotasPro.api.modules.manutencao.mapper.MecanicoMapper;
import br.com.frotasPro.api.modules.manutencao.repository.MecanicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarMecanicosPorOficinaService {

    private final MecanicoRepository mecanicoRepository;

    @Transactional(readOnly = true)
    public Page<MecanicoResponse> listarPorOficina(String codigoOficina, Pageable pageable) {
        return mecanicoRepository.findByOficina_Codigo(codigoOficina, pageable)
                .map(MecanicoMapper::toResponse);
    }
}
