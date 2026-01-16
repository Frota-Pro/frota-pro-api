package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ListarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;

    @Transactional(readOnly = true)
    public Page<CaminhaoResponse> listar(Boolean ativo, String q, Pageable pageable) {
        return caminhaoRepository.search(ativo, q, pageable).map(CaminhaoMapper::toResponse);
    }
}
