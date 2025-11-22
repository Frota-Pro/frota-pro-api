package br.com.frotasPro.api.service.grupoConta;

import br.com.frotasPro.api.controller.response.GrupoContaResponse;
import br.com.frotasPro.api.domain.GrupoConta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.GrupoContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.frotasPro.api.mapper.GrupoContaMapper.toResponse;

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
