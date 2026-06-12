package br.com.frotasPro.api.modules.financeiro.service;

import br.com.frotasPro.api.modules.financeiro.dto.request.ContaRequest;
import br.com.frotasPro.api.modules.financeiro.dto.response.ContaResponse;
import br.com.frotasPro.api.modules.financeiro.domain.Conta;
import br.com.frotasPro.api.modules.financeiro.domain.GrupoConta;
import br.com.frotasPro.api.modules.financeiro.mapper.ContaMapper;
import br.com.frotasPro.api.modules.financeiro.repository.ContaRepository;
import br.com.frotasPro.api.modules.financeiro.repository.GrupoContaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
@RequiredArgsConstructor
public class CriarContaService {

    private final ContaRepository repository;
    private final GrupoContaRepository grupoContaRepository;

    public ContaResponse criar(ContaRequest request) {

        GrupoConta grupo = grupoContaRepository.findByGrupoContaPorCodigoOuCodigoExterno(request.getGrupoConta())
                .orElseThrow(() -> new ObjectNotFound(
                        "Grupo conta não encontrada"
                ));

        Conta conta = Conta.builder()
                .codigo(request.getCodigo())
                .codigoExterno(request.getCodigoExterno())
                .nome(request.getNome())
                .grupoConta(grupo)
                .build();

        repository.save(conta);

        return ContaMapper.toResponse(conta);
    }
}

