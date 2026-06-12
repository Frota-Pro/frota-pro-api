package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.modules.frota.domain.CategoriaCaminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.modules.frota.mapper.CategoriaCaminhaoMapper;
import br.com.frotasPro.api.modules.frota.repository.CategoriaCaminhaoRepository;
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
