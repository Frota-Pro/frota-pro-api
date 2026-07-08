package br.com.frotasPro.api.modules.financeiro.service;

import br.com.frotasPro.api.modules.financeiro.dto.request.GrupoContaRequest;
import br.com.frotasPro.api.modules.financeiro.dto.response.GrupoContaResponse;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.financeiro.domain.GrupoConta;
import br.com.frotasPro.api.modules.financeiro.mapper.GrupoContaMapper;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.modules.financeiro.repository.GrupoContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AtualizarGrupoContaService {

    private final GrupoContaRepository repository;
    private final CaminhaoRepository caminhaoRepository;

    public GrupoContaResponse atualizar(UUID id, GrupoContaRequest request) {

        GrupoConta grupo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Grupo conta não encontrada"));

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCodigo())
                .orElseThrow(() -> new ObjectNotFound("Caminhao não encontrador"));

        grupo.setCodigo(request.getCodigo());
        grupo.setCodigoExterno(request.getCodigoExterno());
        grupo.setNome(request.getNome());
        grupo.setCaminhao(caminhao);

        repository.save(grupo);

        return GrupoContaMapper.toResponse(grupo);
    }
}
