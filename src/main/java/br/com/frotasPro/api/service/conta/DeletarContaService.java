package br.com.frotasPro.api.service.conta;

import br.com.frotasPro.api.domain.Conta;
import br.com.frotasPro.api.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DeletarContaService {

    private final ContaRepository repository;

    public void deletar(UUID id) {

        Conta conta = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Conta não encontrada"
                ));

        repository.delete(conta);
    }
}

