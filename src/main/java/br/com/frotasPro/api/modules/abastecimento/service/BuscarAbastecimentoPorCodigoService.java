package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResponse;
import br.com.frotasPro.api.modules.abastecimento.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentoPorCodigoService {

    private final AbastecimentoRepository repository;

    @Cacheable("abastecimento_buscar_codigo")
    public AbastecimentoResponse buscar(String codigo) {
        return repository.findBycodigo(codigo)
                .map(AbastecimentoMapper::toResponse)
                .orElseThrow(() -> new ObjectNotFound("Abastecimento não encontrado"));
    }
}
