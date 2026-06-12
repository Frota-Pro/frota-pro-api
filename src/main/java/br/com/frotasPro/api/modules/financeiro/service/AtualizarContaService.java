package br.com.frotasPro.api.modules.financeiro.service;

import br.com.frotasPro.api.modules.financeiro.dto.request.ContaRequest;
import br.com.frotasPro.api.modules.financeiro.dto.response.ContaResponse;
import br.com.frotasPro.api.modules.financeiro.domain.Conta;
import br.com.frotasPro.api.modules.financeiro.domain.GrupoConta;
import br.com.frotasPro.api.modules.financeiro.repository.ContaRepository;
import br.com.frotasPro.api.modules.financeiro.repository.GrupoContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.modules.financeiro.mapper.ContaMapper.toResponse;
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

        GrupoConta grupo = grupoContaRepository.findByGrupoContaPorCodigoOuCodigoExterno(request.getGrupoConta())
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
