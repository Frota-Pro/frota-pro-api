package br.com.frotasPro.api.service.grupoConta;

import br.com.frotasPro.api.controller.request.GrupoContaRequest;
import br.com.frotasPro.api.controller.response.GrupoContaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.GrupoConta;
import br.com.frotasPro.api.mapper.GrupoContaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.GrupoContaRepository;
import br.com.frotasPro.api.validator.ValidaSeCaminhaoExiste;
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
    private final ValidaSeCaminhaoExiste validaSeCaminhaoExiste;

    public GrupoContaResponse atualizar(UUID id, GrupoContaRequest request) {

        GrupoConta grupo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Grupo conta não encontrada"));

        validaSeCaminhaoExiste.validar(request.getCaminhaoId());
        Caminhao caminhao = caminhaoRepository.findById(request.getCaminhaoId()).get();

        grupo.setCodigo(request.getCodigo());
        grupo.setCodigoExterno(request.getCodigoExterno());
        grupo.setNome(request.getNome());
        grupo.setCaminhao(caminhao);

        repository.save(grupo);

        return GrupoContaMapper.toResponse(grupo);
    }
}
