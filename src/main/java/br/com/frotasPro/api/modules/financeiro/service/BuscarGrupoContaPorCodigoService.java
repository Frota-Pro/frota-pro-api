package br.com.frotasPro.api.modules.financeiro.service;

import br.com.frotasPro.api.modules.financeiro.dto.response.GrupoContaResponse;
import br.com.frotasPro.api.modules.financeiro.domain.GrupoConta;
import br.com.frotasPro.api.modules.financeiro.repository.GrupoContaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.frotasPro.api.modules.financeiro.mapper.GrupoContaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class BuscarGrupoContaPorCodigoService {

    private final GrupoContaRepository repository;

    public GrupoContaResponse buscarPorCodigo(String codigo) {

        GrupoConta grupo = repository.findByGrupoContaPorCodigoOuCodigoExterno(codigo)
                .orElseThrow(() -> new ObjectNotFound("Grupo conta não encontrada"));

        return toResponse(grupo);
    }
}
