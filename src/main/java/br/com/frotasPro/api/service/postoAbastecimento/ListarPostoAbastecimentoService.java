package br.com.frotasPro.api.service.postoAbastecimento;

import br.com.frotasPro.api.controller.response.PostoAbastecimentoResponse;
import br.com.frotasPro.api.domain.PostoAbastecimento;
import br.com.frotasPro.api.mapper.PostoAbastecimentoMapper;
import br.com.frotasPro.api.repository.PostoAbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarPostoAbastecimentoService {

    private final PostoAbastecimentoRepository repository;

    @Transactional(readOnly = true)
    public Page<PostoAbastecimentoResponse> listar(Boolean ativo, String q, Pageable pageable) {
        return repository.search(ativo, q, pageable).map(PostoAbastecimentoMapper::toResponse);
    }

    /** Lista enxuta de postos ativos, sem paginação — usada pelo seletor do app do motorista. */
    @Transactional(readOnly = true)
    public List<PostoAbastecimentoResponse> listarAtivos() {
        return repository.findAllByAtivoTrueOrderByNomeAsc().stream()
                .map(PostoAbastecimentoMapper::toResponse)
                .toList();
    }
}
