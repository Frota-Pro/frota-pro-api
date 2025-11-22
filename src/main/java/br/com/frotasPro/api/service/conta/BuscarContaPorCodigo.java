package br.com.frotasPro.api.service.conta;

import br.com.frotasPro.api.controller.response.ContaResponse;
import br.com.frotasPro.api.domain.Conta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.frotasPro.api.mapper.ContaMapper.toResponse;

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
