package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentoPorCodigoService {

    private final AbastecimentoRepository repository;

    public AbastecimentoResponse buscar(String codigo) {
        return repository.findBycodigo(codigo)
                .map(AbastecimentoMapper::toResponse)
                .orElseThrow(() -> new ObjectNotFound("Abastecimento não encontrado"));
    }
}

