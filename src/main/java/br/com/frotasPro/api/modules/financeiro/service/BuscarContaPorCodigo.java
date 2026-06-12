package br.com.frotasPro.api.modules.financeiro.service;

import br.com.frotasPro.api.modules.financeiro.dto.response.ContaResponse;
import br.com.frotasPro.api.modules.financeiro.domain.Conta;
import br.com.frotasPro.api.modules.financeiro.repository.ContaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.frotasPro.api.modules.financeiro.mapper.ContaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class BuscarContaPorCodigo {

    private final ContaRepository repository;

    public ContaResponse buscarPorcodigo(String codigo) {

        Conta conta = repository.findByPorCodigoOuCodigoEsterno(codigo)
                .orElseThrow(() -> new ObjectNotFound(
                        "Conta não encontrada"
                ));

        return toResponse(conta);
    }
}
