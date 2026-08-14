package br.com.frotasPro.api.service.postoAbastecimento;

import br.com.frotasPro.api.controller.response.PostoAbastecimentoResponse;
import br.com.frotasPro.api.domain.PostoAbastecimento;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.PostoAbastecimentoMapper;
import br.com.frotasPro.api.repository.PostoAbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarPostoAbastecimentoService {

    private final PostoAbastecimentoRepository repository;

    public PostoAbastecimentoResponse buscarPorCodigo(String codigo) {

        PostoAbastecimento entity = repository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Posto de abastecimento não encontrado para o código: " + codigo));

        return PostoAbastecimentoMapper.toResponse(entity);
    }
}
