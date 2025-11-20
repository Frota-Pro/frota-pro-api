package br.com.frotasPro.api.service.conta;

import br.com.frotasPro.api.controller.response.ContaResponse;
import br.com.frotasPro.api.domain.Conta;
import br.com.frotasPro.api.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.ContaMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BuscarContaPorIdService {

    private final ContaRepository repository;

    public ContaResponse buscarPorId(UUID id) {

        Conta conta = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Conta não encontrada"
                ));

        return toResponse(conta);
    }
}
