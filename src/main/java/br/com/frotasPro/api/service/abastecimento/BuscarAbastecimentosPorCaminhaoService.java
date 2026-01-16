package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentosPorCaminhaoService {

    private final AbastecimentoRepository repository;

    @Transactional(readOnly = true)
    public Page<AbastecimentoResponse> buscar(String codigoCaminhao, Pageable pageable) {
        return repository.buscarPorCodigoCaminhao(codigoCaminhao, pageable)
                .map(AbastecimentoMapper::toResponse);
    }
}
