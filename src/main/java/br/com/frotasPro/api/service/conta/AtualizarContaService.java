package br.com.frotasPro.api.service.conta;

import br.com.frotasPro.api.controller.request.ContaRequest;
import br.com.frotasPro.api.controller.response.ContaResponse;
import br.com.frotasPro.api.domain.Conta;
import br.com.frotasPro.api.domain.GrupoConta;
import br.com.frotasPro.api.repository.ContaRepository;
import br.com.frotasPro.api.repository.GrupoContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.ContaMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AtualizarContaService {

    private final ContaRepository repository;
    private final GrupoContaRepository grupoContaRepository;

    public ContaResponse atualizar(UUID id, ContaRequest request) {

        Conta conta = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Conta não encontrada"
                ));

        GrupoConta grupo = grupoContaRepository.findById(request.getGrupoContaId())
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Grupo conta não encontrada"
                ));

        conta.setCodigo(request.getCodigo());
        conta.setCodigoExterno(request.getCodigoExterno());
        conta.setNome(request.getNome());
        conta.setGrupoConta(grupo);

        repository.save(conta);

        return toResponse(conta);
    }
}
