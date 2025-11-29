package br.com.frotasPro.api.service.categoriaCaminhao;

import br.com.frotasPro.api.controller.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CategoriaCaminhaoMapper;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarCategoriaCaminhaoService {

    private final CategoriaCaminhaoRepository repository;

    public CategoriaCaminhaoResponse buscarPorCodigo(String codigo) {

        CategoriaCaminhao entity = repository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Categoria de caminhão não encontrada para o id: " + codigo));

        return CategoriaCaminhaoMapper.toResponse(entity);
    }
}
